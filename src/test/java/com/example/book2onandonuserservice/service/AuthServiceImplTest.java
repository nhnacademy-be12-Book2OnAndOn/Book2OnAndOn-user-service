package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.RefreshTokenRepository;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.impl.AuthServiceImpl;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.global.service.EmailService;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UserGradeRepository userGradeRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RedisUtil redisUtil;

    @Mock
    private EmailService emailService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PaycoClient paycoClient;

    private UserGrade defaultGrade;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        defaultGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);

        when(userGradeRepository.findByGradeName(GradeName.BASIC))
                .thenReturn(Optional.of(defaultGrade));
    }

    // ================================
    //   회원가입 테스트 (signUp)
    // ================================
    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto(
                "testId",
                "1111",
                "홍길동",
                "test@test.com",
                "01012341234",
                LocalDate.of(2000, 1, 1)
        );

        when(usersRepository.existsByUserLoginId("testId")).thenReturn(false);
        when(usersRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey("test@test.com");
        when(redisUtil.getData(verifiedKey)).thenReturn("true");
        when(passwordEncoder.encode("1111")).thenReturn("encodedPW");

        Users saved = new Users(
                "testId", "encodedPW", "홍길동", "test@test.com",
                "01012341234", LocalDate.of(2000, 1, 1),
                defaultGrade
        );

        when(usersRepository.save(any())).thenReturn(saved);

        UserResponseDto response = authService.signUp(request);

        assertThat(response).isNotNull();
        verify(rabbitTemplate).convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                saved.getUserId()
        );
        verify(userAuthRepository).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_emailDuplicate() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto(
                "testId", "pw", "홍길동",
                "hello@test.com", "01012341234",
                LocalDate.of(2000, 1, 1)
        );

        when(usersRepository.findByEmail("hello@test.com"))
                .thenReturn(Optional.of(mock(Users.class)));

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    // ================================
    //   로그인 테스트 (login)
    // ================================
    @Test
    @DisplayName("로컬 로그인 성공")
    void login_success() {
        LoginRequestDto request = new LoginRequestDto("testId", "pw");

        Users user = mock(Users.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getPassword()).thenReturn("encodedPW");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getStatus()).thenReturn(Status.ACTIVE);

        UserAuth auth = UserAuth.builder()
                .provider("local")
                .providerUserId("testId")
                .user(user)
                .build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "testId"))
                .thenReturn(Optional.of(auth));

        when(passwordEncoder.matches("pw", "encodedPW")).thenReturn(true);

        TokenResponseDto tokens = new TokenResponseDto("access", "refresh", "Bearer", 3600L);

        when(jwtTokenProvider.createTokens(any())).thenReturn(tokens);

        TokenResponseDto response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        LoginRequestDto request = new LoginRequestDto("testId", "wrong");

        UserAuth auth = mock(UserAuth.class);
        Users user = mock(Users.class);

        when(auth.getUser()).thenReturn(user);
        when(userAuthRepository.findByProviderAndProviderUserId("local", "testId"))
                .thenReturn(Optional.of(auth));

        when(user.getPassword()).thenReturn("encodedPW");
        when(passwordEncoder.matches("wrong", "encodedPW"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    // ================================
    // 로그아웃 테스트
    // ================================
    @Test
    @DisplayName("로그아웃 성공 - 블랙리스트 저장 및 Refresh 삭제")
    void logout_success() {
        when(jwtTokenProvider.getExpiration("access")).thenReturn(System.currentTimeMillis() + 50000);
        when(jwtTokenProvider.getUserId("access")).thenReturn("1");

        authService.logout("access");

        verify(redisUtil).setBlackList(any(), any(), anyLong());
        verify(refreshTokenRepository).deleteById("1");
    }

    // ================================
    // 이메일 인증 테스트
    // ================================
    @Test
    @DisplayName("이메일 인증번호 전송 성공")
    void sendEmailCode_success() {
        when(usersRepository.findByEmail("abc@test.com"))
                .thenReturn(Optional.empty());

        authService.sendVerificationCode("abc@test.com");

        verify(emailService).sendMail(any(), any(), any());
        verify(redisUtil).setData(any(), any(), anyLong());
    }

}
