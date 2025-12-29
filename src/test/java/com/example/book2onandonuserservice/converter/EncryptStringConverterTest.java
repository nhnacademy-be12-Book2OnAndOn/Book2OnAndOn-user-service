package com.example.book2onandonuserservice.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
import com.example.book2onandonuserservice.global.exception.DecryptionException;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EncryptStringConverterTest {

    @Mock
    EncryptionUtils encryptionUtils;

    EncryptStringConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new EncryptStringConverter(encryptionUtils);
    }

    // convertToDatabaseColumn() 테스트
    @Test
    void convertToDatabaseColumn_success() {
        String input = "hello";
        String encrypted = "ENCRYPTED";

        when(encryptionUtils.encrypt(input)).thenReturn(encrypted);

        String result = converter.convertToDatabaseColumn(input);

        assertEquals(encrypted, result);
        verify(encryptionUtils).encrypt(input);
    }

    @Test
    void convertToDatabaseColumn_nullInput() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_emptyInput() {
        assertNull(converter.convertToDatabaseColumn(""));
        assertNull(converter.convertToDatabaseColumn("   "));
    }

    // convertToEntityAttribute() 테스트
    @Test
    void convertToEntityAttribute_success() {
        String encrypted = "ENCRYPTED";
        String decrypted = "hello";

        when(encryptionUtils.decrypt(encrypted)).thenReturn(decrypted);

        String result = converter.convertToEntityAttribute(encrypted);

        assertEquals(decrypted, result);
        verify(encryptionUtils).decrypt(encrypted);
    }

    @Test
    void convertToEntityAttribute_decryptExceptionShouldThrow() {
        when(encryptionUtils.decrypt("BAD"))
                .thenThrow(new DecryptionException("복호화 중 오류가 발생했습니다."));

        assertThrows(DecryptionException.class, () -> {
            converter.convertToEntityAttribute("BAD");
        });
    }
}