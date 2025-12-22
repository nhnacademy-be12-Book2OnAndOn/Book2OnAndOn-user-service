package com.example.book2onandonuserservice.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private String privateKeyStr;
    private String publicKeyStr;
    private final long accessValidity = 3600;
    private final long refreshValidity = 7200;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // 테스트용 RSA 키 쌍 생성
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Base64 인코딩하여 문자열로 변환
        privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        jwtTokenProvider = new JwtTokenProvider(privateKeyStr, publicKeyStr, accessValidity, refreshValidity);
    }

    @Test
    @DisplayName("토큰 생성 및 정보 추출 성공")
    void createTokens_And_ExtractClaims_Success() {
        Long userId = 1L;
        String role = "USER";
        TokenRequestDto request = new TokenRequestDto(userId, role);

        TokenResponseDto tokens = jwtTokenProvider.createTokens(request);

        assertThat(tokens.accessToken()).isNotNull();
        assertThat(tokens.refreshToken()).isNotNull();
        assertThat(tokens.tokenType()).isEqualTo("Bearer");

        String extractedUserId = jwtTokenProvider.getUserId(tokens.accessToken());
        String extractedRole = jwtTokenProvider.getRole(tokens.accessToken());

        assertThat(extractedUserId).isEqualTo(String.valueOf(userId));
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void validateToken_Valid() {
        TokenResponseDto tokens = jwtTokenProvider.createTokens(new TokenRequestDto(1L, "USER"));

        boolean isValid = jwtTokenProvider.validateToken(tokens.accessToken());

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 서명(다른 키로 서명된) 토큰")
    void validateToken_Invalid_Signature() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair otherKeyPair = keyGen.generateKeyPair();

        String fakeToken = Jwts.builder()
                .setSubject("1")
                .signWith(otherKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        boolean isValid = jwtTokenProvider.validateToken(fakeToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 만료된 토큰")
    void validateToken_Expired() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(privateKeyStr, publicKeyStr, -1, -1);

        TokenResponseDto tokens = expiredProvider.createTokens(new TokenRequestDto(1L, "USER"));

        boolean isValid = jwtTokenProvider.validateToken(tokens.accessToken());

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 형식의 토큰")
    void validateToken_Malformed() {
        String malformedToken = "Bearer.invalid.token";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료 시간 조회")
    void getExpiration_Success() {
        TokenResponseDto tokens = jwtTokenProvider.createTokens(new TokenRequestDto(1L, "USER"));
        long now = new Date().getTime();

        long expiration = jwtTokenProvider.getExpiration(tokens.accessToken());

        assertThat(expiration).isGreaterThan(now);
    }

    @Test
    @DisplayName("만료된 토큰에서도 정보를 추출할 수 있어야 함 (parseClaims 예외 처리 테스트)")
    void getUserId_From_ExpiredToken_Success() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(privateKeyStr, publicKeyStr, -1, -1);

        TokenRequestDto request = new TokenRequestDto(999L, "USER");
        TokenResponseDto tokens = expiredProvider.createTokens(request);

        String userId = expiredProvider.getUserId(tokens.accessToken());

        assertThat(userId).isEqualTo("999");
    }
}