//package com.example.book2onandonuserservice.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
//import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
//import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
//import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
//import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
//import com.example.book2onandonuserservice.auth.service.AuthTokenService;
//import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
//import com.example.book2onandonuserservice.global.client.PaycoClient;
//import com.example.book2onandonuserservice.point.service.PointHistoryService;
//import com.example.book2onandonuserservice.user.domain.entity.Status;
//import com.example.book2onandonuserservice.user.domain.entity.Users;
//import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
//import com.example.book2onandonuserservice.user.repository.UsersRepository;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//class PaycoAuthServiceTest {
//
//    @InjectMocks
//    private PaycoAuthService paycoAuthService;
//
//    @Mock
//    private PaycoClient paycoClient;
//    @Mock
//    private UserAuthRepository userAuthRepository;
//    @Mock
//    private UsersRepository usersRepository;
//    @Mock
//    private UserGradeRepository userGradeRepository;
//    @Mock
//    private PointHistoryService pointHistoryService;
//    @Mock
//    private AuthTokenService authTokenService; // 위임된 토큰 서비스
//
//    @BeforeEach
//    void setup() {
//        ReflectionTestUtils.setField(paycoAuthService, "paycoClientId", "test-client-id");
//        ReflectionTestUtils.setField(paycoAuthService, "paycoClientSecret", "test-client-secret");
//    }
//
//    @Test
//    @DisplayName("PAYCO 로그인 성공 - 기존 연동 유저")
//    void login_existingUser_success() {
//        PaycoLoginRequestDto request = new PaycoLoginRequestDto("authCode");
//
//        PaycoTokenResponse tokenResponse = mock(PaycoTokenResponse.class);
//        when(tokenResponse.accessToken()).thenReturn("payco-access");
//        when(paycoClient.getToken(any(), any(), any(), any())).thenReturn(tokenResponse);
//
//        PaycoMemberResponse memberResponse = mock(PaycoMemberResponse.class);
//        PaycoMemberResponse.PaycoHeader header = mock(PaycoMemberResponse.PaycoHeader.class);
//        PaycoMemberResponse.PaycoData data = mock(PaycoMemberResponse.PaycoData.class);
//        PaycoMemberResponse.PaycoMember member = mock(PaycoMemberResponse.PaycoMember.class);
//
//        when(memberResponse.getHeader()).thenReturn(header);
//        when(header.isSuccessful()).thenReturn(true);
//        when(memberResponse.getData()).thenReturn(data);
//        when(data.getMember()).thenReturn(member);
//        when(member.getIdNo()).thenReturn("payco-123");
//        when(paycoClient.getMemberInfo(any(), anyString(), anyString())).thenReturn(memberResponse);
//
//        Users existingUser = mock(Users.class);
//        when(existingUser.getStatus()).thenReturn(Status.ACTIVE);
//
//        UserAuth userAuth = UserAuth.builder()
//                .user(existingUser)
//                .build();
//        when(userAuthRepository.findByProviderAndProviderUserId("payco", "payco-123"))
//                .thenReturn(Optional.of(userAuth));
//
//        TokenResponseDto expectedTokens = new TokenResponseDto("acc", "ref", "Bearer", 3600L);
//        when(authTokenService.issueToken(existingUser)).thenReturn(expectedTokens);
//
//        TokenResponseDto result = paycoAuthService.login(request);
//
//        assertThat(result).isEqualTo(expectedTokens);
//        verify(authTokenService).issueToken(existingUser);
//    }
//}