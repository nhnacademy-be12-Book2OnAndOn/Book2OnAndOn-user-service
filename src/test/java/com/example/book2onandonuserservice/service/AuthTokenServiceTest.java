package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.auth.domain.dto.request.ReissueRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.exception.InvalidRefreshTokenException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.redis.RefreshTokenRepository;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @InjectMocks
    private AuthTokenService authTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RedisUtil redisUtil;

    @Test
    @DisplayName("토큰 발급 성공 - RefreshToken이 저장되어야 한다")
    void issueToken_Success() {
        Long userId = 1L;
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "role", Role.USER);

        TokenResponseDto expectedResponse = new TokenResponseDto(
                "accessToken", "refreshToken", "Bearer", 3600L
        );

        given(jwtTokenProvider.createTokens(any(TokenRequestDto.class)))
                .willReturn(expectedResponse);

        TokenResponseDto result = authTokenService.issueToken(user);

        assertThat(result).isEqualTo(expectedResponse);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueToken_Success() {
        String oldAccessToken = "old-access-token";
        String oldRefreshToken = "old-refresh-token";
        String userId = "1";
        String role = "ROLE_USER";

        ReissueRequestDto requestDto = new ReissueRequestDto(oldAccessToken, oldRefreshToken);

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(oldRefreshToken)).willReturn(userId);

        RefreshToken storedToken = new RefreshToken(userId, oldRefreshToken, System.currentTimeMillis());

        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedToken));
        given(jwtTokenProvider.getRole(oldRefreshToken)).willReturn(role);

        TokenResponseDto newTokens = new TokenResponseDto("new-acc", "new-ref", "Bearer", 3600L);
        given(jwtTokenProvider.createTokens(any(TokenRequestDto.class))).willReturn(newTokens);

        TokenResponseDto result = authTokenService.reissueToken(requestDto);

        assertThat(result.accessToken()).isEqualTo("new-acc");
        assertThat(result.refreshToken()).isEqualTo("new-ref");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 RefreshToken")
    void reissueToken_Fail_InvalidToken() {
        ReissueRequestDto requestDto = new ReissueRequestDto("acc", "invalid-ref");

        given(jwtTokenProvider.validateToken("invalid-ref")).willReturn(false);

        assertThatThrownBy(() -> authTokenService.reissueToken(requestDto))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("유효하지 않은 RefreshToken입니다.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis에 저장된 토큰 없음(만료 등)")
    void reissueToken_Fail_TokenNotFound() {
        ReissueRequestDto requestDto = new ReissueRequestDto("acc", "valid-ref");
        String userId = "1";

        given(jwtTokenProvider.validateToken("valid-ref")).willReturn(true);
        given(jwtTokenProvider.getUserId("valid-ref")).willReturn(userId);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authTokenService.reissueToken(requestDto))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("다시 로그인 하세요.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 요청 토큰과 저장된 토큰 불일치 (RTR 위반)")
    void reissueToken_Fail_TokenMismatch() {
        ReissueRequestDto requestDto = new ReissueRequestDto("acc", "request-ref");
        String userId = "1";

        given(jwtTokenProvider.validateToken("request-ref")).willReturn(true);
        given(jwtTokenProvider.getUserId("request-ref")).willReturn(userId);

        long pastTime = System.currentTimeMillis() - 20000;
        RefreshToken storedToken = new RefreshToken(userId, "different-stored-ref", pastTime);

        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authTokenService.reissueToken(requestDto))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("이미 사용된 토큰입니다. 보안을 위해 로그아웃됩니다.");
    }

    @Test
    @DisplayName("로그아웃 성공 - 토큰 유효시간이 남은 경우 블랙리스트 등록 및 DB 삭제")
    void logout_WithRemainingTime() {
        String accessToken = "valid_access_token";
        String userId = "1";

        long futureExpiration = System.currentTimeMillis() + 3600000;

        given(jwtTokenProvider.getExpiration(accessToken)).willReturn(futureExpiration);
        given(jwtTokenProvider.getUserId(accessToken)).willReturn(userId);

        authTokenService.logout(accessToken);

        verify(redisUtil).setBlackList(anyString(), eq("logout"), anyLong());

        verify(refreshTokenRepository).deleteById(userId);
    }

    @Test
    @DisplayName("로그아웃 성공 - 이미 만료된 토큰인 경우 블랙리스트 등록 건너뜀")
    void logout_ExpiredToken() {
        String accessToken = "expired_access_token";
        String userId = "1";

        long pastExpiration = System.currentTimeMillis() - 1000;

        given(jwtTokenProvider.getExpiration(accessToken)).willReturn(pastExpiration);
        given(jwtTokenProvider.getUserId(accessToken)).willReturn(userId);

        authTokenService.logout(accessToken);

        verify(redisUtil, never()).setBlackList(anyString(), anyString(), anyLong());

        verify(refreshTokenRepository).deleteById(userId);
    }

    @Test
    @DisplayName("로그아웃 - userId 추출 실패 시(토큰 오류 등) 삭제 로직 건너뜀")
    void logout_NoUserId() {
        String accessToken = "invalid_token";
        long futureExpiration = System.currentTimeMillis() + 3600000;

        given(jwtTokenProvider.getExpiration(accessToken)).willReturn(futureExpiration);
        given(jwtTokenProvider.getUserId(accessToken)).willReturn(null);

        authTokenService.logout(accessToken);

        verify(redisUtil).setBlackList(anyString(), eq("logout"), anyLong());

        verify(refreshTokenRepository, never()).deleteById(anyString());
    }
}