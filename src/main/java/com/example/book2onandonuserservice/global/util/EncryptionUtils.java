package com.example.book2onandonuserservice.global.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {
    private final String alg = "AES/CBC/PKCS5Padding"; //암호화 알고리즘
    private final String key;
    private final String iv; //초기화 벡터

    public EncryptionUtils(@Value("${encryption.secret-key}") String key) {
        this.key = key;
        this.iv = key.substring(0, 16);
    }

    //암호화 평문->Base64암호문
    public String encrypt(String text) throws Exception {
        if (text == null) {
            return null;
        }

        Cipher cipher = Cipher.getInstance(alg);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    //복호화 Base64-> 평문
    public String decrypt(String cipherText) throws Exception {
        if (cipherText == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance(alg);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());

        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
