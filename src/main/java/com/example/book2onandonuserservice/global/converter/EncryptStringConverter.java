package com.example.book2onandonuserservice.global.converter;

import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Converter
@RequiredArgsConstructor
public class EncryptStringConverter implements AttributeConverter<String, String> {
    private final EncryptionUtils encryptionUtils;

    //암호화
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return null;
        }
        try {
            return encryptionUtils.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("암호화실패", e);
        }
    }

    // 복호화
    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return encryptionUtils.decrypt(dbData);
        } catch (Exception e) {
            return null;
        }
    }

}
