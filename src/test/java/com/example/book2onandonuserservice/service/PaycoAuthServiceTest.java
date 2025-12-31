package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.PaycoInfoMissingException;
import com.example.book2onandonuserservice.auth.exception.PaycoServerException;
import com.example.book2onandonuserservice.auth.repository.jpa.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaycoAuthServiceTest {

    @InjectMocks
    private PaycoAuthService paycoAuthService;

    @Mock
    private PaycoClient paycoClient;
    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private UserGradeRepository userGradeRepository;
    @Mock
    private PointHistoryService pointHistoryService;
    @Mock
    private AuthTokenService authTokenService;
    @Mock
    private EncryptionUtils encryptionUtils;

    private PaycoLoginRequestDto loginRequest;
    private PaycoTokenResponse tokenResponse;
    private UserGrade basicGrade;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입
        ReflectionTestUtils.setField(paycoAuthService, "paycoClientId", "test-client-id");
        ReflectionTestUtils.setField(paycoAuthService, "paycoClientSecret", "test-client-secret");

        loginRequest = new PaycoLoginRequestDto("auth-code");
        // Record는 생성자로 생성
        tokenResponse = new PaycoTokenResponse("access-token", "refresh-token", "3600", "Bearer");

        basicGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
    }

    // Helper: PaycoMemberResponse Mock 생성 (String 생일 반환)
    private PaycoMemberResponse createMockMemberResponse(boolean isSuccess, String idNo, String email, String name,
                                                         String mobile, String birthday) {
        PaycoMemberResponse response = mock(PaycoMemberResponse.class);
        PaycoMemberResponse.PaycoHeader header = mock(PaycoMemberResponse.PaycoHeader.class);

        given(response.getHeader()).willReturn(header);
        given(header.isSuccessful()).willReturn(isSuccess);

        if (isSuccess) {
            PaycoMemberResponse.PaycoData data = mock(PaycoMemberResponse.PaycoData.class);
            PaycoMemberResponse.PaycoMember member = mock(PaycoMemberResponse.PaycoMember.class);

            given(response.getData()).willReturn(data);
            given(data.getMember()).willReturn(member);

            given(member.getIdNo()).willReturn(idNo);
            given(member.getEmail()).willReturn(email);
            given(member.getName()).willReturn(name);
            given(member.getMobile()).willReturn(mobile);
            given(member.getBirthday()).willReturn(birthday);
        } else {
            given(header.getResultMessage()).willReturn("PAYCO Error");
        }

        return response;
    }

    @Test
    @DisplayName("로그인 성공 - 이미 연동된 계정 (ACTIVE) + 정보 동기화(Sync) 확인")
    void login_Success_ExistingAuth() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "홍길동",
                "01012345678", "0101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(encryptionUtils.hash("test@test.com")).willReturn("hashed_test@test.com");

        Users user = new Users();
        user.initSocialAccount("홍길동", "홍길동");

        UserAuth existingAuth = UserAuth.builder().user(user).build();
        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(
                Optional.of(existingAuth));

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(user)).willReturn(expectedToken);

        TokenResponseDto result = paycoAuthService.login(loginRequest);

        assertThat(result.accessToken()).isEqualTo("jwt-access");
        verify(usersRepository, never()).save(any());

        assertThat(user.getBirth()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(user.getPhone()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("로그인 실패 - PAYCO 서버 응답 실패")
    void login_Fail_PaycoServerException() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(false, null, null, null, null, null);
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(PaycoServerException.class)
                .hasMessageContaining("PAYCO 로그인 실패");
    }

    @Test
    @DisplayName("로그인 실패 - PAYCO 필수 정보(이메일/이름) 누락")
    void login_Fail_PaycoInfoMissing() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", null, "홍길동", "01012345678",
                "0101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(PaycoInfoMissingException.class);

        verify(userAuthRepository, never()).findByProviderAndProviderUserId(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 성공 - 신규 회원가입 (포인트 적립 및 생일 파싱 확인)")
    void login_Success_NewUser() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "new@test.com", "신규",
                "821012345678", "1231");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        String emailHash = "hashed_new@test.com";
        given(encryptionUtils.hash("new@test.com")).willReturn(emailHash);

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.empty());
        given(usersRepository.findByEmailHash(emailHash)).willReturn(Optional.empty());

        given(userGradeRepository.findByGradeName(GradeName.BASIC)).willReturn(Optional.of(basicGrade));

        Users savedUser = new Users();
        savedUser.initSocialAccount("신규", "신규");
        ReflectionTestUtils.setField(savedUser, "userId", 100L);

        given(usersRepository.save(any(Users.class))).willReturn(savedUser);

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(savedUser)).willReturn(expectedToken);

        paycoAuthService.login(loginRequest);

        verify(usersRepository).save(any(Users.class));
        verify(userAuthRepository).save(any(UserAuth.class));
        verify(pointHistoryService).earnSignupPoint(100L);

        assertThat(savedUser.getPhone()).isEqualTo("01012345678");
        assertThat(savedUser.getBirth()).isEqualTo(LocalDate.of(2020, 12, 31));
    }

    @Test
    @DisplayName("로그인 성공 - 기존 이메일 계정에 연동 (Link) 및 동기화")
    void login_Success_LinkAccount() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "exist@test.com", "기존",
                "01012345678", "0101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        String emailHash = "hashed_exist@test.com";
        given(encryptionUtils.hash("exist@test.com")).willReturn(emailHash);

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.empty());

        Users existingUser = new Users();
        existingUser.initLocalAccount("localId", "pw", "기존", "nick");

        given(usersRepository.findByEmailHash(emailHash)).willReturn(Optional.of(existingUser));

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(existingUser)).willReturn(expectedToken);

        paycoAuthService.login(loginRequest);

        verify(userAuthRepository).save(any(UserAuth.class));
        verify(usersRepository, never()).save(any(Users.class));

        assertThat(existingUser.getBirth()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    @DisplayName("로그인 실패 - 휴면 계정")
    void login_Fail_DormantUser() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "휴면",
                "01012345678", "0101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(encryptionUtils.hash("test@test.com")).willReturn("hashed_test@test.com");

        Users dormantUser = new Users();
        dormantUser.initLocalAccount("dormantId", "password", "휴면", "nick");
        dormantUser.changeStatus(Status.DORMANT);

        UserAuth auth = UserAuth.builder().user(dormantUser).build();

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.of(auth));

        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(UserDormantException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴 계정")
    void login_Fail_WithdrawnUser() {
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "탈퇴",
                "01012345678", "0101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(encryptionUtils.hash("test@test.com")).willReturn("hashed_test@test.com");

        Users withdrawnUser = new Users();
        withdrawnUser.initLocalAccount("withdrawnId", "password", "탈퇴", "nick");
        withdrawnUser.changeStatus(Status.CLOSED);

        UserAuth auth = UserAuth.builder().user(withdrawnUser).build();

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.of(auth));

        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(UserWithdrawnException.class);
    }
}