package com.example.book2onandonuserservice.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.config.EncryptionProperties;
import com.example.book2onandonuserservice.global.exception.CryptoConfigurationException;
import com.example.book2onandonuserservice.global.exception.DecryptionException;
import com.example.book2onandonuserservice.global.exception.EncryptionException;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EncryptionUtilsTest {

    private EncryptionUtils encryptionUtils;
    private EncryptionProperties properties;

    // AES-256을 위한 32바이트 키 (테스트용)
    private final String keyV1 = "11111111111111111111111111111111";
    private final String keyV2 = "22222222222222222222222222222222";
    private final String hashSecret = "adfasdfasdfasdfasdfasdfasdfasdf";

    @BeforeEach
    void setUp() {
        // Mock 객체 생성
        properties = mock(EncryptionProperties.class);

        // 키 설정 (v1, v2)
        Map<String, String> keys = new HashMap<>();
        keys.put("v1", keyV1);
        keys.put("v2", keyV2);

        // Mock 동작 정의
        when(properties.getKeys()).thenReturn(keys);
        when(properties.getActiveVersion()).thenReturn("v2"); // 현재 활성 버전 v2
        when(properties.getHashSecret()).thenReturn(hashSecret);

        // EncryptionUtils 초기화
        encryptionUtils = new EncryptionUtils(properties);
        encryptionUtils.init();
    }

    // ==========================================
    // 1. 정상 동작 테스트 (Happy Path)
    // ==========================================

    @Test
    @DisplayName("암호화 성공 - v2 접두사가 붙고 복호화 시 원문과 같아야 함")
    void encrypt_success() {
        String plainText = "HelloEncryption!";

        String encrypted = encryptionUtils.encrypt(plainText);

        assertThat(encrypted).startsWith("v2:");
        assertThat(encryptionUtils.decrypt(encrypted)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 성공 - v1 접두사가 있는 데이터 (구버전 키로 복호화)")
    void decrypt_v1_prefixed() throws Exception {
        String plainText = "LegacyDataV1";
        // v1 키로 수동 암호화
        String rawCipher = manualEncrypt(plainText, keyV1);
        String dbValue = "v1:" + rawCipher;

        String decrypted = encryptionUtils.decrypt(dbValue);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 성공 - 접두사가 없는 레거시 데이터 (Default v1 적용)")
    void decrypt_legacy_no_prefix() throws Exception {
        String plainText = "OldDataNoPrefix";
        // v1 키로 수동 암호화 (접두사 없음)
        String dbValue = manualEncrypt(plainText, keyV1);

        String decrypted = encryptionUtils.decrypt(dbValue);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("AES-GCM 암호화 시 매번 다른 암호문이 생성됨 (IV 랜덤성 확인)")
    void encrypt_randomIv_generatesDifferentCipherText() {
        String plainText = "SameText";

        String encrypted1 = encryptionUtils.encrypt(plainText);
        String encrypted2 = encryptionUtils.encrypt(plainText);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("HMAC 해시 생성 테스트 (멱등성 확인)")
    void hash_success() {
        String plainText = "test@email.com";

        String hash1 = encryptionUtils.hash(plainText);
        String hash2 = encryptionUtils.hash(plainText);

        assertThat(hash1)
                .isEqualTo(hash2)
                .isNotEqualTo(plainText);
    }

    @Test
    @DisplayName("null 입력 시 null 반환 (Null Safe)")
    void nullInput_returnsNull() {
        assertThat(encryptionUtils.encrypt(null)).isNull();
        assertThat(encryptionUtils.decrypt(null)).isNull();
        assertThat(encryptionUtils.hash(null)).isNull();
    }

    // ==========================================
    // 2. 초기화 설정 관련 테스트 (Init & Config)
    // ==========================================

    @Test
    @DisplayName("init: 키 설정(keys)이 null이거나 비어있으면 예외 발생")
    void init_emptyKeys_throwsException() {
        EncryptionProperties badProps = mock(EncryptionProperties.class);
        when(badProps.getKeys()).thenReturn(Collections.emptyMap()); // 빈 맵

        EncryptionUtils badUtils = new EncryptionUtils(badProps);

        assertThatThrownBy(badUtils::init)
                .isInstanceOf(CryptoConfigurationException.class)
                .hasMessageContaining("암호화 키 설정");
    }

    @Test
    @DisplayName("init: active-version에 해당하는 키가 없으면 예외 발생")
    void init_missingActiveKey_throwsException() {
        EncryptionProperties badProps = mock(EncryptionProperties.class);
        Map<String, String> keys = new HashMap<>();
        keys.put("v1", keyV1); // v1만 존재

        when(badProps.getKeys()).thenReturn(keys);
        when(badProps.getActiveVersion()).thenReturn("v3"); // v3를 활성으로 설정 (없음)

        EncryptionUtils badUtils = new EncryptionUtils(badProps);

        assertThatThrownBy(badUtils::init)
                .isInstanceOf(CryptoConfigurationException.class)
                .hasMessageContaining("설정된 active-version");
    }

    @Test
    @DisplayName("init: 키 길이가 32바이트가 아니어도 에러 없이 통과 (로그만 기록됨)")
    void init_invalidKeyLength_logsWarningButSucceeds() {
        EncryptionProperties warnProps = mock(EncryptionProperties.class);
        Map<String, String> keys = new HashMap<>();
        keys.put("short", "shortKey"); // 32바이트 아님

        when(warnProps.getKeys()).thenReturn(keys);
        when(warnProps.getActiveVersion()).thenReturn("short");

        EncryptionUtils warnUtils = new EncryptionUtils(warnProps);

        assertThatCode(warnUtils::init).doesNotThrowAnyException();
    }

    // ==========================================
    // 3. 재암호화 판단 로직 테스트 (Re-encryption)
    // ==========================================

    @Test
    @DisplayName("needsReEncryption: null이나 빈 문자열은 false")
    void needsReEncryption_nullOrEmpty_returnsFalse() {
        assertThat(encryptionUtils.needsReEncryption(null)).isFalse();
        assertThat(encryptionUtils.needsReEncryption("")).isFalse();
    }

    @Test
    @DisplayName("needsReEncryption: Active 버전(v2)으로 시작하면 false")
    void needsReEncryption_matchActiveVersion_returnsFalse() {
        String encrypted = "v2:some-encrypted-data";
        assertThat(encryptionUtils.needsReEncryption(encrypted)).isFalse();
    }

    @Test
    @DisplayName("needsReEncryption: 다른 버전(v1)으로 시작하면 true")
    void needsReEncryption_mismatchVersion_returnsTrue() {
        String encrypted = "v1:some-encrypted-data";
        assertThat(encryptionUtils.needsReEncryption(encrypted)).isTrue();
    }

    @Test
    @DisplayName("needsReEncryption: 버전 접두사가 없어도 true (Legacy Data)")
    void needsReEncryption_noPrefix_returnsTrue() {
        String encrypted = "some-encrypted-data-without-prefix";
        assertThat(encryptionUtils.needsReEncryption(encrypted)).isTrue();
    }

    // ==========================================
    // 4. 예외 처리 테스트 (Exceptions)
    // ==========================================

    @Test
    @DisplayName("encrypt: 내부 로직 에러 시 EncryptionException 발생")
    void encrypt_internalError_throwsEncryptionException() {
        // init 이후에 activeVersion이 이상한 값으로 바뀌었다고 가정 (키 맵에 없는 키)
        when(properties.getActiveVersion()).thenReturn("invalid_version");

        assertThatThrownBy(() -> encryptionUtils.encrypt("test"))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("데이터 암호화 중 오류");
    }

    @Test
    @DisplayName("decrypt: 데이터가 변조(Tampered)되었을 때 DecryptionException 발생")
    void decrypt_corruptedData_throwsDecryptionException() {
        String original = encryptionUtils.encrypt("Secret");
        String[] parts = original.split(":", 2);
        String version = parts[0];
        String body = parts[1];

        byte[] decodedBytes = Base64.getDecoder().decode(body);

        decodedBytes[decodedBytes.length - 1] ^= 1;

        String corruptedBody = Base64.getEncoder().encodeToString(decodedBytes);
        String modified = version + ":" + corruptedBody;

        assertThatThrownBy(() -> encryptionUtils.decrypt(modified))
                .isInstanceOf(DecryptionException.class)
                .hasMessageContaining("데이터 복호화 중 치명적인 오류");
    }

    @Test
    @DisplayName("decrypt: 잘못된 Base64 포맷인 경우 DecryptionException 발생")
    void decrypt_invalidCipherText_throwsException() {
        String invalidCipher = "v2:invalid-base64-data!!!!";

        assertThatThrownBy(() -> encryptionUtils.decrypt(invalidCipher))
                .isInstanceOf(DecryptionException.class)
                .hasMessageContaining("복호화 데이터 형식이 잘못되었습니다");
    }

    @Test
    @DisplayName("decrypt: 존재하지 않는 버전의 키로 복호화 시도 시 DecryptionException 발생")
    void decrypt_unknownVersion_throwsException() throws Exception {
        String plainText = "Data";
        String rawCipher = manualEncrypt(plainText, keyV1);
        String dbValue = "v99:" + rawCipher; // 없는 버전 v99

        assertThatThrownBy(() -> encryptionUtils.decrypt(dbValue))
                .isInstanceOf(DecryptionException.class)
                .hasMessageContaining("알 수 없는 암호화 키 버전");
    }

    // ==========================================
    // 5. Helper Method (테스트 보조)
    // ==========================================

    private String manualEncrypt(String text, String keyStr) throws Exception {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        SecretKeySpec keySpec = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
}