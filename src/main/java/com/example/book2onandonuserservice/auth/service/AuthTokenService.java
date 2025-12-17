package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.auth.domain.dto.request.ReissueRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.redis.RefreshTokenRepository;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisUtil redisUtil;

    //토큰 생성
    @Transactional
    public TokenResponseDto issueToken(Users user) {
        TokenRequestDto tokenRequest = new TokenRequestDto(user.getUserId(), user.getRole().getKey());
        TokenResponseDto tokenResponse = jwtTokenProvider.createTokens(tokenRequest);

        RefreshToken refreshToken = new RefreshToken(
                String.valueOf(user.getUserId()),
                tokenResponse.refreshToken()
        );
        refreshTokenRepository.save(refreshToken);

        return tokenResponse;
    }

    // AccessToken 재발급 로직
    public TokenResponseDto reissueToken(ReissueRequestDto request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 RefreshToken입니다.");
        }
        String userId = jwtTokenProvider.getUserId(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh 토큰이 없습니다. 다시 로그인 하세요."));

        if (!storedToken.getToken().equals(request.refreshToken())) {
            throw new IllegalArgumentException("RefreshToken이 일치하지 않습니다.");
        }
        String role = jwtTokenProvider.getRole(request.refreshToken());
        TokenRequestDto tokenRequest = new TokenRequestDto(Long.parseLong(userId), role);

        TokenResponseDto newToken = jwtTokenProvider.createTokens(tokenRequest);

        // Redis에 새 RefreshToken 발급하며 보안강화
        RefreshToken newRefreshToken = new RefreshToken(userId, newToken.refreshToken());
        refreshTokenRepository.save(newRefreshToken);
        return newToken;
    }

    //로그아웃
    @Transactional
    public void logout(String accessToken) {
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        long now = System.currentTimeMillis();
        long remainingTime = expiration - now;

        if (remainingTime > 0) {
            String blackListKey = RedisKeyPrefix.BLACKLIST.buildKey(accessToken);
            redisUtil.setBlackList(blackListKey, "logout", remainingTime);
        }

        String userId = jwtTokenProvider.getUserId(accessToken);
        if (userId != null) {
            refreshTokenRepository.deleteById(userId);
        }
    }

}
