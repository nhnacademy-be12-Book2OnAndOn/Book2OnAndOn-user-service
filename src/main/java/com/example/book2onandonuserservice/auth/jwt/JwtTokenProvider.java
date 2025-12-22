package com.example.book2onandonuserservice.auth.jwt;

import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";

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

    // 인증 성공 시 AccessToken, RefreshToken 생성
    public TokenResponseDto createTokens(TokenRequestDto tokenRequest) {
        String accessToken = createAccessToken(tokenRequest);
        String refreshToken = createRefreshToken(tokenRequest);

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenValidityInMilliseconds / 1000
        );
    }

    private String createAccessToken(TokenRequestDto tokenRequest) {
        Claims claims = Jwts.claims();
        claims.put(KEY_USER_ID, tokenRequest.userId());
        claims.put(KEY_ROLE, tokenRequest.role());

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(TokenRequestDto tokenRequest) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        String jti = UUID.randomUUID().toString();

        Claims claims = Jwts.claims();
        claims.put(KEY_USER_ID, tokenRequest.userId());
        claims.put(KEY_ROLE, tokenRequest.role());
        claims.put("jti", jti);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserId(String token) {
        return parseClaims(token).get(KEY_USER_ID).toString();
    }

    public String getRole(String token) {
        return parseClaims(token).get(KEY_ROLE, String.class);
    }

    public long getExpiration(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Date getIssuedAt(String token) {
        return parseClaims(token).getIssuedAt();
    }
}