package com.example.book2onandonuserservice.global.util;

import com.example.book2onandonuserservice.global.config.EncryptionProperties;
import com.example.book2onandonuserservice.global.exception.CryptoConfigurationException;
import com.example.book2onandonuserservice.global.exception.DecryptionException;
import com.example.book2onandonuserservice.global.exception.EncryptionException;
import com.example.book2onandonuserservice.global.exception.HashingException;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionUtils {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final String HMAC_ALG = "HmacSHA256";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final String SEPARATOR = ":";

    private final EncryptionProperties properties;
    private Map<String, SecretKey> secretKeyMap;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostConstruct
    public void init() {
        secretKeyMap = new HashMap<>();
        Map<String, String> keyConfig = properties.getKeys();
        String activeVersion = properties.getActiveVersion();

        if (keyConfig == null || keyConfig.isEmpty()) {
            throw new CryptoConfigurationException(
                    "암호화 키 설정(encryption.keys)이 존재하지 않습니다. application.properties를 확인해주세요.");
        }

        for (Map.Entry<String, String> entry : keyConfig.entrySet()) {
            String version = entry.getKey();
            String keyStr = entry.getValue();

            if (keyStr.getBytes(StandardCharsets.UTF_8).length != 32) {
                log.warn("경고: 키 버전({})의 길이가 32바이트(AES-256)가 아닙니다.", version);
            }
            secretKeyMap.put(version, new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES"));
        }

        if (!secretKeyMap.containsKey(activeVersion)) {
            throw new CryptoConfigurationException("설정된 active-version (" + activeVersion + ")에 해당하는 암호화 키가 없습니다.");
        }
        log.info("EncryptionUtils 초기화 완료. Active Version: {}", activeVersion);
    }

    public String encrypt(String text) {
        if (text == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_SIZE];
            SECURE_RANDOM.nextBytes(iv);

            String activeVersion = properties.getActiveVersion();
            SecretKey key = secretKeyMap.get(activeVersion);

            Cipher cipher = Cipher.getInstance(ALG);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            String encodedData = Base64.getEncoder().encodeToString(byteBuffer.array());

            return activeVersion + SEPARATOR + encodedData;

        } catch (Exception e) {
            throw new EncryptionException("데이터 암호화 중 오류가 발생했습니다.", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        try {
            String version;
            String actualCipherBase64;

            if (cipherText.contains(SEPARATOR)) {
                String[] parts = cipherText.split(SEPARATOR, 2);
                version = parts[0];
                actualCipherBase64 = parts[1];
            } else {
                version = "v1";
                actualCipherBase64 = cipherText;
            }

            SecretKey key = secretKeyMap.get(version);
            if (key == null) {
                throw new DecryptionException("알 수 없는 암호화 키 버전입니다: " + version);
            }

            byte[] decoded = Base64.getDecoder().decode(actualCipherBase64);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);

            byte[] encrypted = new byte[decoded.length - IV_SIZE];
            byteBuffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALG);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (DecryptionException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new DecryptionException("복호화 데이터 형식이 잘못되었습니다 (Base64 등).", e);
        } catch (Exception e) {
            throw new DecryptionException("데이터 복호화 중 치명적인 오류가 발생했습니다.", e);
        }
    }

    public boolean needsReEncryption(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return false;
        }
        String activeVersion = properties.getActiveVersion();
        return !cipherText.startsWith(activeVersion + SEPARATOR);
    }

    public String hash(String text) {
        if (text == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    properties.getHashSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALG
            );
            mac.init(secretKeySpec);

            byte[] encodedHash = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HashingException("해시 값 생성 중 오류가 발생했습니다.", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}