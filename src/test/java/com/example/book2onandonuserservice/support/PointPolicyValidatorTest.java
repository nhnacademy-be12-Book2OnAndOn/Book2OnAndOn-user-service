package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.support.pointpolicy.PointPolicyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointPolicyValidatorTest {

    private final PointPolicyValidator validator = new PointPolicyValidator();

    @Test
    @DisplayName("validatePoint: 정상 케이스(0 이상)면 예외 발생 x")
    void validatePoint_success() {
        assertAll(
                () -> assertDoesNotThrow(() -> validator.validatePoint(0)),
                () -> assertDoesNotThrow(() -> validator.validatePoint(10))
        );
    }

    @Test
    @DisplayName("validatePoint: null이면 InvalidPointPolicyException 발생")
    void validatePoint_null_throws() {
        InvalidPointPolicyException ex = assertThrows(
                InvalidPointPolicyException.class,
                () -> validator.validatePoint(null)
        );

        assertAll(
                () -> assertNotNull(ex.getMessage()),
                () -> assertTrue(ex.getMessage().contains("고정 포인트는 반드시 설정해야 합니다."))
        );
    }

    @Test
    @DisplayName("validatePoint: 음수면 InvalidPointPolicyException 발생")
    void validatePoint_negative_throws() {
        InvalidPointPolicyException ex = assertThrows(
                InvalidPointPolicyException.class,
                () -> validator.validatePoint(-1)
        );

        assertAll(
                () -> assertNotNull(ex.getMessage()),
                () -> assertTrue(ex.getMessage().contains("0 이상"))
        );
    }
}
