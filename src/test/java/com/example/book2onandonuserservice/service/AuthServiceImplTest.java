package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
        when(userGradeRepository.findByGradeName(GradeName.BASIC)).thenReturn(Optional.of(defaultGrade));
    }

    // 회원가입 (SignUp) 관련 테스트
    @Test
    @DisplayName("회원가입 성공 - 정상 케이스")
    void signUp_success() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto(
                "testId", "1111", "홍길동", "test@test.com", "01012341234", LocalDate.of(2000, 1, 1));

        when(usersRepository.existsByUserLoginId("testId")).thenReturn(false);
        when(usersRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey("test@test.com");
        when(redisUtil.getData(verifiedKey)).thenReturn("true");

        when(passwordEncoder.encode("1111")).thenReturn("encodedPW");

        Users savedUser = new Users("testId", "encodedPW", "홍길동", "test@test.com", "01012341234",
                LocalDate.of(2000, 1, 1), defaultGrade);
        ReflectionTestUtils.setField(savedUser, "userId", 100L); // RabbitMQ 전송 확인용 ID 주입

        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);

        UserResponseDto response = authService.signUp(request);

        assertThat(response).isNotNull();
        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, 100L);
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복 (existsByUserLoginId 커버)")
    void signUp_fail_idDuplicate() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("dupId", "pw", "name", "e@e.com", "010",
                LocalDate.now());

        when(usersRepository.existsByUserLoginId("dupId")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserLoginIdDuplicateException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 (findByEmail.isPresent 커버)")
    void signUp_fail_emailDuplicate() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("newId", "pw", "name", "dup@e.com", "010",
                LocalDate.now());

        when(usersRepository.existsByUserLoginId("newId")).thenReturn(false);
        when(usersRepository.findByEmail("dup@e.com")).thenReturn(Optional.of(mock(Users.class)));

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 인증 미완료 (Redis check 커버)")
    void signUp_fail_notVerified() {
        LocalSignUpRequestDto request = new LocalSignUpRequestDto("newId", "pw", "name", "unverified@e.com", "010",
                LocalDate.now());

        when(usersRepository.existsByUserLoginId("newId")).thenReturn(false);
        when(usersRepository.findByEmail("unverified@e.com")).thenReturn(Optional.empty());

        // Redis에 인증 정보 없음 (null)
        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey("unverified@e.com");
        when(redisUtil.getData(verifiedKey)).thenReturn(null);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 인증이 완료되지 않았습니다.");
    }

    // 로그인 관련 테스트

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequestDto request = new LoginRequestDto("testId", "pw");

        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().provider("local").providerUserId("testId").user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "testId")).thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encodedPW");
        when(user.getStatus()).thenReturn(Status.ACTIVE); // 정상 상태
        when(user.getUserId()).thenReturn(1L);
        when(user.getRole()).thenReturn(Role.USER);

        when(passwordEncoder.matches("pw", "encodedPW")).thenReturn(true);

        TokenResponseDto tokens = new TokenResponseDto("access", "refresh", "Bearer", 3600L);
        when(jwtTokenProvider.createTokens(any())).thenReturn(tokens);

        authService.login(request);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_password() {
        LoginRequestDto request = new LoginRequestDto("testId", "wrongPw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId(anyString(), anyString())).thenReturn(
                Optional.of(auth));
        when(user.getPassword()).thenReturn("encodedPW");
        when(passwordEncoder.matches("wrongPw", "encodedPW")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 휴면 계정 (Status.DORMANT 커버)")
    void login_fail_dormant() {
        LoginRequestDto request = new LoginRequestDto("dormant", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId(anyString(), anyString())).thenReturn(
                Optional.of(auth));
        when(user.getPassword()).thenReturn("encodedPW");
        when(passwordEncoder.matches("pw", "encodedPW")).thenReturn(true);

        when(user.getStatus()).thenReturn(Status.DORMANT);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserDormantException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴 계정 (Status.CLOSED 커버)")
    void login_fail_closed() {
        LoginRequestDto request = new LoginRequestDto("closed", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId(anyString(), anyString())).thenReturn(
                Optional.of(auth));
        when(user.getPassword()).thenReturn("encodedPW");
        when(passwordEncoder.matches("pw", "encodedPW")).thenReturn(true);

        when(user.getStatus()).thenReturn(Status.CLOSED);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserWithdrawnException.class);
    }

    // 로그아웃 (Logout) 관련 테스트

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        String accessToken = "access_token";
        when(jwtTokenProvider.getExpiration(accessToken)).thenReturn(System.currentTimeMillis() + 10000);
        when(jwtTokenProvider.getUserId(accessToken)).thenReturn("user1");

        authService.logout(accessToken);

        verify(redisUtil).setBlackList(anyString(), eq("logout"), anyLong());
        verify(refreshTokenRepository).deleteById("user1");
    }

    // 이메일 인증 관련 테스트

    @Test
    @DisplayName("인증번호 발송 성공 (SecureRandom 및 Redis 저장 확인)")
    void sendVerificationCode_success() {
        String email = "valid@test.com";
        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        authService.sendVerificationCode(email);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(redisUtil).setData(anyString(), captor.capture(), anyLong());
        String code = captor.getValue();

        assertThat(code).hasSize(6).containsOnlyDigits();
        verify(emailService).sendMail(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("인증번호 발송 실패 - 이미 가입된 이메일")
    void sendVerificationCode_fail_duplicate() {
        String email = "dup@test.com";
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(mock(Users.class)));

        assertThatThrownBy(() -> authService.sendVerificationCode(email))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    @Test
    @DisplayName("인증번호 검증 성공 (Redis 값 일치)")
    void verifyEmail_success() {
        String email = "test@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(email);

        when(redisUtil.getData(key)).thenReturn(code);

        boolean result = authService.verifyEmail(email, code);

        assertThat(result).isTrue();
        verify(redisUtil).setData(eq(RedisKeyPrefix.EMAIL_VERIFIED.buildKey(email)), eq("true"), anyLong());
    }

    @Test
    @DisplayName("인증번호 검증 실패 (Redis 값 불일치)")
    void verifyEmail_fail() {
        String email = "test@test.com";
        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(email);

        when(redisUtil.getData(key)).thenReturn("123456");

        boolean result = authService.verifyEmail(email, "000000"); // 틀린 코드

        assertThat(result).isFalse();
    }

    // 아이디 찾기 관련 테스트

    @Test
    @DisplayName("아이디 찾기 성공 - 정상적인 ID 마스킹 확인 (substring 커버)")
    void findId_success_masking() {
        FindIdRequestDto request = new FindIdRequestDto("홍길동", "test@test.com");
        Users user = mock(Users.class);

        when(usersRepository.findByNameAndEmail("홍길동", "test@test.com")).thenReturn(Optional.of(user));

        when(user.getUserLoginId()).thenReturn("testUser");

        FindIdResponseDto response = authService.findId(request);

        assertThat(response.userLoginId()).isEqualTo("testUs**");
    }

    @Test
    @DisplayName("아이디 찾기 예외 - 아이디가 없거나 공백인 경우 (빈 문자열 반환 커버)")
    void findId_masking_empty() {
        FindIdRequestDto request = new FindIdRequestDto("홍길동", "test@test.com");
        Users user = mock(Users.class);

        when(usersRepository.findByNameAndEmail("홍길동", "test@test.com")).thenReturn(Optional.of(user));

        when(user.getUserLoginId()).thenReturn("");

        FindIdResponseDto response = authService.findId(request);

        assertThat(response.userLoginId()).isEmpty();
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 회원 없음")
    void findId_fail_notFound() {
        FindIdRequestDto request = new FindIdRequestDto("없는사람", "no@e.com");
        when(usersRepository.findByNameAndEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.findId(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("입력하신 정보와 일치하는 회원이 없습니다.");
    }

    // 임시 비밀번호 발급 관련 테스트

    @Test
    @DisplayName("임시 비밀번호 발급 성공")
    void issueTempPassword_success() {
        FindPasswordRequestDto request = new FindPasswordRequestDto("id", "email");
        Users user = mock(Users.class);

        when(usersRepository.findByUserLoginIdAndEmail("id", "email")).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("email");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPw");

        authService.issueTemporaryPassword(request);

        verify(user).changePassword("encodedTempPw");
        verify(emailService).sendMail(eq("email"), anyString(), anyString());
    }
}