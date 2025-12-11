package com.example.book2onandonuserservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.auth.service.AuthVerificationService;
import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
import com.example.book2onandonuserservice.auth.service.impl.AuthServiceImpl;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthTokenService authTokenService;
    @Mock
    private AuthVerificationService verificationService;
    @Mock
    private PaycoAuthService paycoAuthService;

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UserGradeRepository userGradeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PointHistoryService pointHistoryService;
    @Mock
    private RedisUtil redisUtil;

    private UserGrade defaultGrade;

    @BeforeEach
    void setup() {
        defaultGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
    }


    @Test
    @DisplayName("PAYCO 로그인 - PaycoAuthService확인")
    void loginWithPayco_delegates() {
        PaycoLoginRequestDto request = new PaycoLoginRequestDto("code");
        authService.loginWithPayco(request);
        verify(paycoAuthService).login(request);
    }

    @Test
    @DisplayName("이메일 인증번호 발송 - VerificationService확인")
    void sendVerificationCode_delegates() {
        String email = "test@test.com";
        authService.sendVerificationCode(email);
        verify(verificationService).sendVerificationCode(email);
    }


    @Test
    @DisplayName("로컬 회원가입 성공")
    void signUp_success() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto(
                "testId",
                "1111",
                "홍길동",
                "test@test.com",
                "TestNick",
                "01012341234",
                LocalDate.of(2000, 1, 1)
        );
        when(usersRepository.existsByUserLoginId("testId")).thenReturn(false);

        when(usersRepository.existsByNickname("TestNick")).thenReturn(false);

        when(usersRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        String redisKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey("test@test.com");
        when(redisUtil.getData(redisKey)).thenReturn("true");

        when(userGradeRepository.findByGradeName(GradeName.BASIC)).thenReturn(Optional.of(defaultGrade));
        when(passwordEncoder.encode("1111")).thenReturn("encodedPW");

        Users savedUser = new Users();
        savedUser.initLocalAccount("testId", "encodedPW", "홍길동", "TestNick");
        savedUser.setContactInfo("test@test.com", "01012341234", LocalDate.of(2000, 1, 1));
        savedUser.changeGrade(defaultGrade);
        ReflectionTestUtils.setField(savedUser, "userId", 1L);

        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);

        authService.signUp(request);

        verify(usersRepository).save(any(Users.class));
        verify(pointHistoryService).earnSignupPoint(1L);
        verify(userAuthRepository).save(any(UserAuth.class));
    }

    @Test
    @DisplayName("로컬 로그인 성공")
    void login_success() {
        LoginRequestDto request = new LoginRequestDto("testId", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "testId")).thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encoded");
        when(user.getStatus()).thenReturn(Status.ACTIVE);
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);

        authService.login(request);

        verify(authTokenService).issueToken(user);
    }
}