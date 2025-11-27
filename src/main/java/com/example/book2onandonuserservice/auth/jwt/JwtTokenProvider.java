package com.example.book2onandonuserservice.auth.jwt;

import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final Key secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secret,
                            @Value("${jwt.access-token-validity}") long accessValidity,
                            @Value("${jwt.refresh-token-validity}") long refreshValidity) {
        byte[] keyBytes = secret.getBytes();

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenValidityInMilliseconds = accessValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshValidity * 1000;
    }

    //인증 성공 시 AccessToken, RefreshToken 생성
    public TokenResponseDto createTokens(TokenRequestDto tokenRequest) {
        String accessToken = createAccessToken(tokenRequest);
        String refreshToken = createRefreshToken();

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenValidityInMilliseconds / 1000
        );
    }

    private String createAccessToken(TokenRequestDto tokenRequest) {
        Claims claims = Jwts.claims();
        claims.put("userId", tokenRequest.userId());
        claims.put("role", tokenRequest.role());

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

}
