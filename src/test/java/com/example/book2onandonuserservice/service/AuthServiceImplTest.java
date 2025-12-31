package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.ReissueRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.repository.jpa.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.auth.service.AuthVerificationService;
import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
import com.example.book2onandonuserservice.auth.service.impl.AuthServiceImpl;
import com.example.book2onandonuserservice.global.event.EmailSendEvent;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.EmailNotVerifiedException;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;
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
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EncryptionUtils encryptionUtils; // [추가]

    private UserGrade defaultGrade;

    @BeforeEach
    void setup() {
        defaultGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
    }

    // 위임 메서드 테스트 (Coverage 확보용)
    @Test
    @DisplayName("위임 메서드 검증")
    void delegateMethods_check() {
        authService.sendVerificationCode("a@a.com");
        verify(verificationService).sendVerificationCode("a@a.com");

        authService.sendDormantVerificationCode("a@a.com");
        verify(verificationService).sendDormantVerificationCode("a@a.com");

        when(verificationService.verifyEmail("a@a.com", "123")).thenReturn(true);
        assertThat(authService.verifyEmail("a@a.com", "123")).isTrue();

        authService.unlockDormantAccount("a@a.com", "123");
        verify(verificationService).unlockDormantAccount("a@a.com", "123");

        PaycoLoginRequestDto paycoReq = new PaycoLoginRequestDto("code");
        authService.loginWithPayco(paycoReq);
        verify(paycoAuthService).login(paycoReq);

        authService.logout("accessToken");
        verify(authTokenService).logout("accessToken");
    }

    // 토큰 재발급 테스트
    @Test
    @DisplayName("재발급 성공 - AuthTokenService에 위임하고 결과 반환")
    void reissue_success() {
        ReissueRequestDto request = new ReissueRequestDto("oldAccessToken", "oldRefreshToken");
        TokenResponseDto expected = mock(TokenResponseDto.class);

        when(authTokenService.reissueToken(request)).thenReturn(expected);

        TokenResponseDto result = authService.reissue(request);

        assertThat(result).isSameAs(expected);
        verify(authTokenService).reissueToken(request);
    }

    @Test
    @DisplayName("재발급 실패 - AuthTokenService에서 예외 발생 시 그대로 전파")
    void reissue_fail_propagateException() {
        ReissueRequestDto request = new ReissueRequestDto("oldAccessToken", "oldRefreshToken");

        when(authTokenService.reissueToken(request))
                .thenThrow(new RuntimeException("토큰 재발급 실패"));

        assertThatThrownBy(() -> authService.reissue(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("토큰 재발급 실패");
    }


    // 로컬 회원가입 테스트
    @Test
    @DisplayName("회원가입 성공 - 포인트 적립 실패해도 가입은 진행됨")
    void signUp_success_evenIfPointFails() {
        LocalSignUpRequestDto request = createSignUpRequest();
        String fakeHash = "hashedEmail"; // [추가]

        // [수정] 해시 생성 Mock
        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash);

        when(usersRepository.existsByUserLoginId(request.userLoginId())).thenReturn(false);
        when(usersRepository.existsByNickname(request.nickname())).thenReturn(false);
        // [수정] 해시값으로 중복 조회 Mock
        when(usersRepository.findByEmailHash(fakeHash)).thenReturn(Optional.empty());

        String redisKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(request.email());
        when(redisUtil.getData(redisKey)).thenReturn("true");

        when(userGradeRepository.findByGradeName(GradeName.BASIC)).thenReturn(Optional.of(defaultGrade));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPW");

        Users savedUser = new Users();
        savedUser.initLocalAccount(request.userLoginId(), "encodedPW", request.name(), request.nickname());
        ReflectionTestUtils.setField(savedUser, "userId", 1L);

        savedUser.changeGrade(defaultGrade);

        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);

        doThrow(new RuntimeException("Point Error")).when(pointHistoryService).earnSignupPoint(1L);

        UserResponseDto response = authService.signUp(request);

        assertThat(response).isNotNull();
        verify(usersRepository).save(any(Users.class));
        verify(userAuthRepository).save(any(UserAuth.class));
        verify(redisUtil).deleteData(redisKey);
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signUp_fail_duplicateId() {
        LocalSignUpRequestDto request = createSignUpRequest();
        when(encryptionUtils.hash(anyString())).thenReturn("hash"); // [추가]
        when(usersRepository.existsByUserLoginId(request.userLoginId())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserLoginIdDuplicateException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signUp_fail_duplicateNickname() {
        LocalSignUpRequestDto request = createSignUpRequest();
        when(encryptionUtils.hash(anyString())).thenReturn("hash"); // [추가]
        when(usersRepository.existsByUserLoginId(request.userLoginId())).thenReturn(false);
        when(usersRepository.existsByNickname(request.nickname())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserNicknameDuplicationException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicateEmail() {
        LocalSignUpRequestDto request = createSignUpRequest();
        String fakeHash = "hashedEmail";

        // [수정] 해시 생성 및 중복 조회 설정
        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash);
        when(usersRepository.existsByUserLoginId(request.userLoginId())).thenReturn(false);
        when(usersRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(usersRepository.findByEmailHash(fakeHash)).thenReturn(Optional.of(new Users()));

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 미인증")
    void signUp_fail_emailNotVerified() {
        LocalSignUpRequestDto request = createSignUpRequest();
        String fakeHash = "hashedEmail";

        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash); // [추가]
        when(usersRepository.existsByUserLoginId(request.userLoginId())).thenReturn(false);
        when(usersRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(usersRepository.findByEmailHash(fakeHash)).thenReturn(Optional.empty()); // [수정]

        String redisKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(request.email());
        when(redisUtil.getData(redisKey)).thenReturn(null);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    @DisplayName("회원가입 실패 - 기본 등급(BASIC) 데이터 없음")
    void signUp_fail_noDefaultGrade() {
        LocalSignUpRequestDto request = createSignUpRequest();
        String fakeHash = "hashedEmail";

        when(encryptionUtils.hash(anyString())).thenReturn(fakeHash); // [추가]
        when(usersRepository.existsByUserLoginId(any())).thenReturn(false);
        when(usersRepository.existsByNickname(any())).thenReturn(false);
        when(usersRepository.findByEmailHash(fakeHash)).thenReturn(Optional.empty()); // [수정]
        when(redisUtil.getData(any())).thenReturn("true");

        when(userGradeRepository.findByGradeName(GradeName.BASIC)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalStateException.class);
    }

    // 로컬 로그인 테스트
    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequestDto request = new LoginRequestDto("id", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "id"))
                .thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(user.getStatus()).thenReturn(Status.ACTIVE);

        // When
        authService.login(request);

        // Then
        verify(user).updateLastLogin();
        verify(authTokenService).issueToken(user);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_fail_notFound() {
        LoginRequestDto request = new LoginRequestDto("id", "pw");
        when(userAuthRepository.findByProviderAndProviderUserId("local", "id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_passwordMismatch() {
        LoginRequestDto request = new LoginRequestDto("id", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "id"))
                .thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 휴면 계정")
    void login_fail_dormant() {
        LoginRequestDto request = new LoginRequestDto("id", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "id"))
                .thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(user.getStatus()).thenReturn(Status.DORMANT);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserDormantException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴한 계정")
    void login_fail_withdrawn() {
        LoginRequestDto request = new LoginRequestDto("id", "pw");
        Users user = mock(Users.class);
        UserAuth auth = UserAuth.builder().user(user).build();

        when(userAuthRepository.findByProviderAndProviderUserId("local", "id"))
                .thenReturn(Optional.of(auth));
        when(user.getPassword()).thenReturn("encoded");
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(user.getStatus()).thenReturn(Status.CLOSED);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserWithdrawnException.class);
    }

    // 아이디 찾기 테스트
    @Test
    @DisplayName("아이디 찾기 성공 - 긴 아이디 마스킹 확인")
    void findId_success_longId() {
        FindIdRequestDto request = new FindIdRequestDto("홍길동", "a@a.com");
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userLoginId", "testUser1234");
        String fakeHash = "hashedEmail";

        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash); // [추가]
        // [수정] 해시값으로 조회
        when(usersRepository.findByNameAndEmailHash("홍길동", fakeHash))
                .thenReturn(Optional.of(user));

        FindIdResponseDto response = authService.findMemberIdByNameAndEmail(request);

        assertThat(response.userLoginId()).isEqualTo("testUser12**"); // 뒤 2자리 마스킹
    }

    @Test
    @DisplayName("아이디 찾기 성공 - 짧은 아이디 마스킹 안함 (2글자 이하)")
    void findId_success_shortId() {
        FindIdRequestDto request = new FindIdRequestDto("홍길동", "a@a.com");
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userLoginId", "ab");
        String fakeHash = "hashedEmail";

        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash); // [추가]
        // [수정] 해시값으로 조회
        when(usersRepository.findByNameAndEmailHash("홍길동", fakeHash))
                .thenReturn(Optional.of(user));

        FindIdResponseDto response = authService.findMemberIdByNameAndEmail(request);

        assertThat(response.userLoginId()).isEqualTo("ab"); // 마스킹 없음
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 회원 없음")
    void findId_fail_notFound() {
        FindIdRequestDto request = new FindIdRequestDto("홍길동", "a@a.com");
        when(encryptionUtils.hash(anyString())).thenReturn("hash"); // [추가]
        when(usersRepository.findByNameAndEmailHash(any(), any())).thenReturn(Optional.empty()); // [수정]

        assertThatThrownBy(() -> authService.findMemberIdByNameAndEmail(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    // 5. 임시 비밀번호 발급 테스트
    @Test
    @DisplayName("임시 비밀번호 발급 성공")
    void issueTemporaryPassword_success() {
        FindPasswordRequestDto request = new FindPasswordRequestDto("id", "a@a.com");
        Users user = mock(Users.class);
        String fakeHash = "hashedEmail";

        when(encryptionUtils.hash(request.email())).thenReturn(fakeHash); // [추가]
        // [수정] 해시값으로 조회
        when(usersRepository.findByUserLoginIdAndEmailHash("id", fakeHash))
                .thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("a@a.com"); // [주의] 이메일 발송 위해 필요
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTemp");

        authService.issueTemporaryPassword(request);

        verify(user).changePassword("encodedTemp");
        verify(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("임시 비밀번호 발급 실패 - 회원 없음")
    void issueTemporaryPassword_fail_notFound() {
        FindPasswordRequestDto request = new FindPasswordRequestDto("id", "a@a.com");
        when(encryptionUtils.hash(anyString())).thenReturn("hash"); // [추가]
        when(usersRepository.findByUserLoginIdAndEmailHash(any(), any())) // [수정]
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.issueTemporaryPassword(request))
                .isInstanceOf(UserNotFoundException.class);
    }


    private LocalSignUpRequestDto createSignUpRequest() {
        return new LocalSignUpRequestDto(
                "testId", "1111", "홍길동", "test@test.com", "Nick", "01012345678", LocalDate.now()
        );
    }
}