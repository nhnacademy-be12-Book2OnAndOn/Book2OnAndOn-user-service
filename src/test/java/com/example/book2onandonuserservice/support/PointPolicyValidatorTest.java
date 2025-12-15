package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.support.pointpolicy.PointPolicyValidator;
import org.junit.jupiter.api.Test;

class PointPolicyValidatorTest {

    private final PointPolicyValidator validator = new PointPolicyValidator();

    @Test
    void validatePoint_success() {
        assertDoesNotThrow(() -> validator.validatePoint(10));
    }

    @Test
    void validatePoint_null_throws() {
        InvalidPointPolicyException ex =
                assertThrows(InvalidPointPolicyException.class,
                        () -> validator.validatePoint(null));

        assertTrue(ex.getMessage().contains("반드시 설정"));
    }

    @Test
    void validatePoint_negative_throws() {
        InvalidPointPolicyException ex =
                assertThrows(InvalidPointPolicyException.class,
                        () -> validator.validatePoint(-1));

        assertTrue(ex.getMessage().contains("0 이상"));
    }
}
