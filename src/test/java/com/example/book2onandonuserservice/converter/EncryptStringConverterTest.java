package com.example.book2onandonuserservice.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
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
    void convertToDatabaseColumn_success() throws Exception {
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

    @Test
    void convertToDatabaseColumn_encryptException() throws Exception {
        when(encryptionUtils.encrypt("test"))
                .thenThrow(new RuntimeException("암호화오류"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> converter.convertToDatabaseColumn("test"));

        assertTrue(ex.getMessage().contains("암호화실패"));
    }

    // convertToEntityAttribute() 테스트
    @Test
    void convertToEntityAttribute_success() throws Exception {
        String encrypted = "ENCRYPTED";
        String decrypted = "hello";

        when(encryptionUtils.decrypt(encrypted)).thenReturn(decrypted);

        String result = converter.convertToEntityAttribute(encrypted);

        assertEquals(decrypted, result);
        verify(encryptionUtils).decrypt(encrypted);
    }

    @Test
    void convertToEntityAttribute_decryptExceptionReturnsNull() throws Exception {
        when(encryptionUtils.decrypt("BAD"))
                .thenThrow(new RuntimeException("복호화 실패"));

        String result = converter.convertToEntityAttribute("BAD");

        assertNull(result);
    }
}