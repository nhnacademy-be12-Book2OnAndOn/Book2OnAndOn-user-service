package com.example.book2onandonuserservice.global.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BIT = 128;

    private final SecretKeySpec keySpec;

    public EncryptionUtils(@Value("${encryption.secret-key}") String key) {
        this.keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }

    // 암호화
    public String encrypt(String text) throws Exception {
        if (text == null) {
            return null;
        }

        // 랜덤 IV 생성
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Cipher 초기화
        Cipher cipher = Cipher.getInstance(ALG);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        // 암호화 실행
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        // IV + 암호문을 Base64로 합쳐 저장
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);

        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    // 복호화
    public String decrypt(String cipherText) throws Exception {
        if (cipherText == null) {
            return null;
        }

        byte[] decoded = Base64.getDecoder().decode(cipherText);

        // 저장된 IV 추출
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[IV_SIZE];
        byteBuffer.get(iv);

        // 암호문 추출
        byte[] encrypted = new byte[decoded.length - IV_SIZE];
        byteBuffer.get(encrypted);

        // 복호화 초기화
        Cipher cipher = Cipher.getInstance(ALG);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        // 복호화 실행
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
