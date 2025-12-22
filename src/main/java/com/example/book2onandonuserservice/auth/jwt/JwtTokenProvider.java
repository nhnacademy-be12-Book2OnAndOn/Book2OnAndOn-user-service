package com.example.book2onandonuserservice.auth.jwt;

import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.exception.JwtKeyInitializationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "role";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.private-key}") String privateKeyStr,
                            @Value("${jwt.public-key}") String publicKeyStr,
                            @Value("${jwt.access-token-validity}") long accessValidity,
                            @Value("${jwt.refresh-token-validity}") long refreshValidity) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // Private Key 로딩 (PKCS#8 포맷, Base64 인코딩 가정)
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            // Public Key 로딩 (X.509 포맷, Base64 인코딩 가정)
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        } catch (Exception e) {
            throw new JwtKeyInitializationException("JWT Key initialization failed", e);
        }
        this.accessTokenValidityInMilliseconds = accessValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshValidity * 1000;
    }

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
                .signWith(privateKey, SignatureAlgorithm.RS256) // PrivateKey로 서명
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
                .signWith(privateKey, SignatureAlgorithm.RS256) // PrivateKey로 서명
                .compact();
    }

    // ... getUserId, getRole, getExpiration, getIssuedAt 메소드는 동일하게 parseClaims를 사용하므로 변경 불필요 ...

    public String getUserId(String token) {
        return parseClaims(token).get(KEY_USER_ID).toString();
    }

    public String getRole(String token) {
        return parseClaims(token).get(KEY_ROLE, String.class);
    }

    public long getExpiration(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    public Date getIssuedAt(String token) {
        return parseClaims(token).getIssuedAt();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token); // PublicKey로 검증
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey) // PublicKey로 검증
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}