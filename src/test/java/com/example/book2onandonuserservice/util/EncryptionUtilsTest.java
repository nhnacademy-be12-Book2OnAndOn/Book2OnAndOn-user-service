package com.example.book2onandonuserservice.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EncryptionUtilsTest {

    private final String secretKey = "1234567890123456"; // 16바이트 AES key
    private final EncryptionUtils encryptionUtils = new EncryptionUtils(secretKey);

    @Test
    @DisplayName("AES-GCM 암호화/복호화 성공")
    void encryptDecrypt_success() throws Exception {
        String plainText = "HelloEncryption!";

        String encrypted = encryptionUtils.encrypt(plainText);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("AES-GCM 암호화 시 매번 다른 암호문이 생성됨(IV 랜덤 때문)")
    void encrypt_randomIv_generatesDifferentCipherText() throws Exception {
        String plainText = "SameText";

        String encrypted1 = encryptionUtils.encrypt(plainText);
        String encrypted2 = encryptionUtils.encrypt(plainText);

        // Base64 문자열이 서로 달라야 함
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("null 입력 시 null 반환 - encrypt")
    void encrypt_nullInput_returnsNull() throws Exception {
        assertThat(encryptionUtils.encrypt(null)).isNull();
    }

    @Test
    @DisplayName("null 입력 시 null 반환 - decrypt")
    void decrypt_nullInput_returnsNull() throws Exception {
        assertThat(encryptionUtils.decrypt(null)).isNull();
    }

    @Test
    @DisplayName("잘못된 암호문 복호화 시 예외 발생")
    void decrypt_invalidCipherText_throwsException() {
        String invalidCipher = "this-is-not-valid-base64";

        assertThatThrownBy(() -> encryptionUtils.decrypt(invalidCipher))
                .isInstanceOf(Exception.class);
    }
}
