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
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
import com.example.book2onandonuserservice.global.client.PaycoClient;
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

    // Helper: PaycoMemberResponse Mock 생성
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
    @DisplayName("로그인 성공 - 이미 연동된 계정 (ACTIVE)")
    void login_Success_ExistingAuth() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "홍길동",
                "01012345678", "20000101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        Users user = new Users();
        user.initSocialAccount("홍길동", "홍길동");
        // status ACTIVE 기본값

        UserAuth existingAuth = UserAuth.builder().user(user).build();
        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(
                Optional.of(existingAuth));

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(user)).willReturn(expectedToken);

        // When
        TokenResponseDto result = paycoAuthService.login(loginRequest);

        // Then
        assertThat(result.accessToken()).isEqualTo("jwt-access");
        verify(usersRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 실패 - PAYCO 서버 응답 실패")
    void login_Fail_PaycoServerException() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(false, null, null, null, null, null);
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        // When & Then
        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(PaycoServerException.class)
                .hasMessageContaining("PAYCO 로그인 실패");
    }

    @Test
    @DisplayName("로그인 실패 - PAYCO 필수 정보(이메일/이름) 누락")
    void login_Fail_PaycoInfoMissing() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        // 이메일 없음
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", null, "홍길동", "01012345678",
                "20000101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(PaycoInfoMissingException.class);
    }

    @Test
    @DisplayName("로그인 성공 - 신규 회원가입 (포인트 적립 포함)")
    void login_Success_NewUser() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        // 전화번호 형식 변환 테스트 (8210... -> 010...)
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "new@test.com", "신규",
                "821012345678", "19991231");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.empty());
        given(usersRepository.findByEmail("new@test.com")).willReturn(Optional.empty());

        given(userGradeRepository.findByGradeName(GradeName.BASIC)).willReturn(Optional.of(basicGrade));

        Users savedUser = new Users();
        savedUser.initSocialAccount("신규", "신규");
        ReflectionTestUtils.setField(savedUser, "userId", 100L); // ID 주입 for Point Service

        given(usersRepository.save(any(Users.class))).willReturn(savedUser);

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(savedUser)).willReturn(expectedToken);

        // When
        paycoAuthService.login(loginRequest);

        // Then
        verify(usersRepository).save(any(Users.class)); // 유저 저장 확인
        verify(userAuthRepository).save(any(UserAuth.class)); // 연동 정보 저장 확인
        verify(pointHistoryService).earnSignupPoint(100L); // 포인트 적립 확인
    }

    @Test
    @DisplayName("로그인 성공 - 기존 이메일 계정에 연동 (Link)")
    void login_Success_LinkAccount() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);

        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "exist@test.com", "기존",
                "01012345678", "19991231");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.empty());

        Users existingUser = new Users();
        existingUser.initLocalAccount("localId", "pw", "기존", "nick");
        given(usersRepository.findByEmail("exist@test.com")).willReturn(Optional.of(existingUser));

        TokenResponseDto expectedToken = new TokenResponseDto("jwt-access", "jwt-refresh", "Bearer", 3600L);
        given(authTokenService.issueToken(existingUser)).willReturn(expectedToken);

        // When
        paycoAuthService.login(loginRequest);

        // Then
        verify(userAuthRepository).save(any(UserAuth.class)); // 연동 테이블에 저장되었는지 확인
        verify(usersRepository, never()).save(any(Users.class)); // 유저는 새로 저장 안 함
    }

    @Test
    @DisplayName("로그인 실패 - 휴면 계정")
    void login_Fail_DormantUser() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "휴면",
                "01012345678", "20000101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        Users dormantUser = new Users();
        dormantUser.changeStatus(Status.DORMANT);
        UserAuth auth = UserAuth.builder().user(dormantUser).build();

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.of(auth));

        // When & Then
        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(UserDormantException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴 계정")
    void login_Fail_WithdrawnUser() {
        // Given
        given(paycoClient.getToken(anyString(), anyString(), anyString(), anyString())).willReturn(tokenResponse);
        PaycoMemberResponse memberResponse = createMockMemberResponse(true, "payco-id", "test@test.com", "탈퇴",
                "01012345678", "20000101");
        given(paycoClient.getMemberInfo(any(URI.class), anyString(), anyString())).willReturn(memberResponse);

        Users withdrawnUser = new Users();
        withdrawnUser.changeStatus(Status.CLOSED);
        UserAuth auth = UserAuth.builder().user(withdrawnUser).build();

        given(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-id")).willReturn(Optional.of(auth));

        // When & Then
        assertThatThrownBy(() -> paycoAuthService.login(loginRequest))
                .isInstanceOf(UserWithdrawnException.class);
    }
}