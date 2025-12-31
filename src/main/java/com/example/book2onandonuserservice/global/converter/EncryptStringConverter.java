package com.example.book2onandonuserservice.global.converter;

import com.example.book2onandonuserservice.global.exception.DecryptionException;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Converter
@RequiredArgsConstructor
@Slf4j
public class EncryptStringConverter implements AttributeConverter<String, String> {

    private final EncryptionUtils encryptionUtils;

    // 암호화 (Entity -> DB)
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return null;
        }
        return encryptionUtils.encrypt(attribute);
    }

    // 복호화 (DB -> Entity)
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        try {
            return encryptionUtils.decrypt(dbData);
        } catch (DecryptionException e) {
            log.error("데이터 복호화 실패 (Cipher: {}): {}", dbData, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("데이터 복호화 중 알 수 없는 오류 발생 (Cipher: {})", dbData, e);
            throw new DecryptionException("JPA 변환 중 복호화 오류 발생", e);
        }
    }
}