package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.RefreshTokenRepository;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.impl.AuthServiceImpl;
import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.global.service.EmailService;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
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
    private UsersRepository usersRepository;
    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UserGradeRepository userGradeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @Mock
    private EmailService emailService;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;


    private Users testUser;
    private UserGrade testGrade;
    private UserAuth testAuth;

    @BeforeEach
    void setUp() {
        testGrade = new UserGrade(1L, GradeName.BASIC, 0.0, 100);

        testUser = new Users(
                "testUser",
                "encodedPw",
                "testName",
                "test@test.com",
                "01012345678",
                LocalDate.now(),
                testGrade
        );

        ReflectionTestUtils.setField(testUser, "userId", 1L);
        ReflectionTestUtils.setField(testUser, "status", Status.ACTIVE);
        ReflectionTestUtils.setField(testUser, "lastLoginAt", java.time.LocalDateTime.now());

        testAuth = UserAuth.builder()
                .provider("local")
                .providerUserId("testUser")
                .user(testUser)
                .build();
    }


    // 1. 이메일 인증 관련 테스트
    @Test
    @DisplayName("이메일 인증번호 발송 성공")
    void sendVerificationCode_Success() {
        String email = "new@test.com";
        given(usersRepository.findByEmail(anyString())).willReturn(Optional.empty());

        authService.sendVerificationCode(email);

        verify(redisUtil, times(1)).setData(startsWith("AuthCode:"), anyString(), anyLong());
        verify(emailService, times(1)).sendMail(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("이메일 인증번호 발송 실패 - 이미 존재하는 이메일")
    void sendVerificationCode_Fail_Duplicate() {
        String email = "test@test.com";
        given(usersRepository.findByEmail(email)).willReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.sendVerificationCode(email))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    @Test
    @DisplayName("인증번호 검증 성공")
    void verifyEmail_Success() {
        String email = "test@test.com";
        String code = "123456";
        given(redisUtil.getData("AuthCode:" + email)).willReturn(code);

        boolean result = authService.verifyEmail(email, code);

        assertThat(result).isTrue();
        verify(redisUtil).setData("Verified:" + email, "true", 30 * 60 * 1000L);
        verify(redisUtil).deleteData("AuthCode:" + email);
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 코드 불일치")
    void verifyEmail_Fail_Mismatch() {
        given(redisUtil.getData(anyString())).willReturn("123456");

        boolean result = authService.verifyEmail("test@test.com", "999999");

        assertThat(result).isFalse();
    }

    // 2. 회원가입 관련 테스트
    @Test
    @DisplayName("로컬 회원가입 성공")
    void signUp_Success() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("newUser", "pw", "name", "new@test.com",
                "01012341234", LocalDate.now());

        given(usersRepository.existsByUserLoginId(anyString())).willReturn(false);
        given(usersRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(redisUtil.getData("Verified:" + request.email())).willReturn("true"); // 인증 완료 상태 가정
        given(userGradeRepository.findByGradeName(GradeName.BASIC)).willReturn(Optional.of(testGrade));
        given(passwordEncoder.encode(anyString())).willReturn("encodedPw");
        given(usersRepository.save(any(Users.class))).willReturn(testUser);

        UserResponseDto response = authService.signUp(request);

        assertThat(response).isNotNull();
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.ROUTING_KEY), anyLong());
        verify(userAuthRepository).save(any(UserAuth.class));
        verify(redisUtil).deleteData("Verified:" + request.email());
    }

    @Test
    @DisplayName("로컬 회원가입 실패 - 아이디 중복")
    void signUp_Fail_DuplicateId() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("testUser", "pw", "name", "new@test.com",
                "01012341234", LocalDate.now());
        given(usersRepository.existsByUserLoginId(request.userLoginId())).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserLoginIdDuplicateException.class);
    }

    @Test
    @DisplayName("로컬 회원가입 실패 - 이메일 인증 미완료")
    void signUp_Fail_NotVerified() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("newUser", "pw", "name", "new@test.com",
                "01012341234", LocalDate.now());
        given(usersRepository.existsByUserLoginId(request.userLoginId())).willReturn(false);
        given(usersRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(redisUtil.getData("Verified:" + request.email())).willReturn(null); // 인증 안됨

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 인증이 완료되지 않았습니다.");
    }

    @Test
    @DisplayName("기본 등급 데이터 없음 예외")
    void signUp_Fail_NoDefaultGrade() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("newUser", "pw", "name", "new@test.com",
                "01012341234", LocalDate.now());
        given(redisUtil.getData(anyString())).willReturn("true");
        given(userGradeRepository.findByGradeName(GradeName.BASIC)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB에 없습니다");
    }

    // 3. 로그인 관련 테스트 (일반 로그인)
    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequestDto request = new LoginRequestDto("testUser", "password");
        given(userAuthRepository.findByProviderAndProviderUserId("local", "testUser")).willReturn(
                Optional.of(testAuth));
        given(passwordEncoder.matches("password", "encodedPw")).willReturn(true);

        TokenResponseDto mockToken = new TokenResponseDto("access", "refresh", "Bearer", 3600L);
        given(jwtTokenProvider.createTokens(any())).willReturn(mockToken);

        // when
        TokenResponseDto result = authService.login(request);

        // then
        assertThat(result).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        LoginRequestDto request = new LoginRequestDto("testUser", "wrongPw");
        given(userAuthRepository.findByProviderAndProviderUserId("local", "testUser")).willReturn(
                Optional.of(testAuth));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 휴면 계정")
    void login_Fail_Dormant() {
        Users dormantUser = new Users(
                "dormant", "pw", "name", "email", "phone", LocalDate.now(), testGrade
        );
        ReflectionTestUtils.setField(dormantUser, "userId", 2L);
        ReflectionTestUtils.setField(dormantUser, "status", Status.DORMANT);

        UserAuth dormantAuth = UserAuth.builder().provider("local").user(dormantUser).build();
        LoginRequestDto request = new LoginRequestDto("dormant", "pw");

        given(userAuthRepository.findByProviderAndProviderUserId(anyString(), anyString())).willReturn(
                Optional.of(dormantAuth));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserDormantException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴 계정")
    void login_Fail_Withdrawn() {

        Users closedUser = new Users(
                "closed", "pw", "name", "email", "phone", LocalDate.now(), testGrade
        );

        ReflectionTestUtils.setField(closedUser, "userId", 3L);
        ReflectionTestUtils.setField(closedUser, "status", Status.CLOSED);

        UserAuth closedAuth = UserAuth.builder().provider("local").user(closedUser).build();
        LoginRequestDto request = new LoginRequestDto("closed", "pw");

        given(userAuthRepository.findByProviderAndProviderUserId(anyString(), anyString())).willReturn(
                Optional.of(closedAuth));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserWithdrawnException.class);
    }

    // ========================================================================================
    // 5. 로그아웃, 아이디 찾기, 비밀번호 찾기 등 기타 테스트
    // ========================================================================================

    @Test
    @DisplayName("로그아웃")
    void logout_Success() {
        String token = "accessToken";
        given(jwtTokenProvider.getExpiration(token)).willReturn(System.currentTimeMillis() + 10000);
        given(jwtTokenProvider.getUserId(token)).willReturn("1");

        authService.logout(token);

        verify(redisUtil).setBlackList(eq(token), eq("logout"), anyLong());
        verify(refreshTokenRepository).deleteById("1");
    }

    @Test
    @DisplayName("아이디 찾기 성공 - 마스킹 확인")
    void findId_Success() {
        given(usersRepository.findByNameAndEmail("testName", "test@test.com")).willReturn(Optional.of(testUser));

        FindIdResponseDto response = authService.findId(new FindIdRequestDto("testName", "test@test.com"));

        assertThat(response.userLoginId()).isEqualTo("testUs**");
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 사용자 없음")
    void findId_Fail_NotFound() {
        given(usersRepository.findByNameAndEmail(anyString(), anyString())).willReturn(Optional.empty());
        FindIdRequestDto request = new FindIdRequestDto("name", "email");

        assertThatThrownBy(() -> authService.findId(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("임시 비밀번호 발급 성공")
    void issueTemporaryPassword_Success() {
        given(usersRepository.findByUserLoginIdAndEmail(anyString(), anyString())).willReturn(Optional.of(testUser));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPw");

        FindPasswordRequestDto request = new FindPasswordRequestDto("id", "email");
        authService.issueTemporaryPassword(request);

        verify(emailService).sendMail(eq(testUser.getEmail()), anyString(), contains("임시 비밀번호"));
    }

}