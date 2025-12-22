package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.auth.domain.dto.request.ReissueRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.exception.InvalidRefreshTokenException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.redis.RefreshTokenRepository;
import com.example.book2onandonuserservice.global.annotation.DistributedLock;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
                tokenResponse.refreshToken(),
                System.currentTimeMillis()
        );
        refreshTokenRepository.save(refreshToken);

        return tokenResponse;
    }

    // AccessToken 재발급 로직
    @DistributedLock(key = "#request.refreshToken")
    public TokenResponseDto reissueToken(ReissueRequestDto request) {

        // 기본 검증 및 로그
        String reqToken = request.refreshToken();
        String reqTokenTail = reqToken.length() > 10 ? reqToken.substring(reqToken.length() - 10) : reqToken;
        log.info("[RTR/분산락] 재발급 요청 진입: {}", reqTokenTail);

        if (!jwtTokenProvider.validateToken(reqToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 RefreshToken입니다.");
        }

        String userId = jwtTokenProvider.getUserId(reqToken);
        RefreshToken storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new InvalidRefreshTokenException("다시 로그인 하세요."));

        // 불일치 감지 (RTR 체크)
        if (!storedToken.getToken().equals(reqToken)) {

            // 토큰이 갱신된 지 얼마나 지났는지 확인
            long timeSinceUpdate = System.currentTimeMillis() - storedToken.getUpdatedAt();

            if (timeSinceUpdate < 10000) {
                log.info("[RTR/분산락] 동시 요청 감지 ({}ms 경과). 최신 토큰 반환.", timeSinceUpdate);

                // 기존 로직: 최신 토큰 반환
                String role = jwtTokenProvider.getRole(storedToken.getToken());
                TokenRequestDto tokenRequest = new TokenRequestDto(Long.parseLong(userId), role);
                TokenResponseDto tempToken = jwtTokenProvider.createTokens(tokenRequest);

                return new TokenResponseDto(
                        tempToken.accessToken(),
                        storedToken.getToken(),
                        tempToken.tokenType(),
                        tempToken.expiresIn()
                );
            } else {
                log.error("[RTR 위반] 이미 사용된 토큰 재사용 감지! ({}ms 경과). 전체 로그아웃 처리.", timeSinceUpdate);

                refreshTokenRepository.delete(storedToken);
                throw new InvalidRefreshTokenException("이미 사용된 토큰입니다. 보안을 위해 로그아웃됩니다.");
            }
        }

        // 새 토큰 생성
        String role = jwtTokenProvider.getRole(reqToken);
        TokenRequestDto tokenRequest = new TokenRequestDto(Long.parseLong(userId), role);
        TokenResponseDto newToken = jwtTokenProvider.createTokens(tokenRequest);

        // DB 업데이트
        RefreshToken newRefreshToken = new RefreshToken(userId, newToken.refreshToken(), System.currentTimeMillis());
        refreshTokenRepository.save(newRefreshToken);

        // [RTR 로그 2] 새로 발급된 토큰 확인
        String newTokenStr = newToken.refreshToken();
        String newTokenTail =
                newTokenStr.length() > 10 ? newTokenStr.substring(newTokenStr.length() - 10) : newTokenStr;
        log.info(" [RTR 완료] 새 토큰 발급 및 저장(New): ...{}", newTokenTail);

        return newToken;
    }

    //로그아웃
    @Transactional
    public void logout(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        long expiration = jwtTokenProvider.getExpiration(accessToken);
        long now = System.currentTimeMillis();
        long remainingTime = expiration - now;

        log.info(" [로그아웃 시도]");
        log.info(" - 토큰 남은 시간(ms): {}", remainingTime);

        if (remainingTime > 0) {
            String blackListKey = RedisKeyPrefix.BLACKLIST.buildKey(accessToken);

            // 확인용 로그: 실제 저장되는 Redis Key 확인
            log.info(" - Redis Key 생성됨: {}", blackListKey);

            redisUtil.setBlackList(blackListKey, "logout", remainingTime);
            log.info(" - 블랙리스트 저장 완료");
        } else {
            log.warn(" - 이미 만료된 토큰입니다. 블랙리스트에 저장하지 않습니다.");
        }

        String userId = jwtTokenProvider.getUserId(accessToken);
        if (userId != null) {
            refreshTokenRepository.deleteById(userId);
            log.info(" - Refresh Token 삭제 완료 (User ID: {})", userId);
        }
    }

}
