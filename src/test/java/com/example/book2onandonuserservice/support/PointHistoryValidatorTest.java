package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.OrderAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.PointAlreadyUsedForOrderException;
import com.example.book2onandonuserservice.point.exception.PointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.ReturnAlreadyProcessedException;
import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryValidator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointHistoryValidatorTest {

    @Mock
    PointHistoryRepository repository;

    @InjectMocks
    PointHistoryValidator validator;

    // 1. 양수 금액 검증
    @Test
    void validatePositiveAmount_success() {
        assertDoesNotThrow(() -> validator.validatePositiveAmount(10, "msg"));
    }

    @Test
    void validatePositiveAmount_fail() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validatePositiveAmount(0, "msg"));
    }

    // 2. 사용 가능 범위 검증
    @Test
    void validatePointRange_success() {
        assertDoesNotThrow(() -> validator.validatePointRange(100, 100));
        assertDoesNotThrow(() -> validator.validatePointRange(50, 100));
    }

    @Test
    void validatePointRange_fail() {
        assertThrows(PointRangeExceededException.class,
                () -> validator.validatePointRange(200, 100));
    }

    // 3. 주문 중복 적립
    @Test
    void validateOrderEarnNotDuplicated_success() {
        when(repository.existsByOrderIdAndPointReason(10L, PointReason.ORDER))
                .thenReturn(false);

        assertDoesNotThrow(() -> validator.validateOrderEarnNotDuplicated(10L));
    }

    @Test
    void validateOrderEarnNotDuplicated_throw() {
        when(repository.existsByOrderIdAndPointReason(10L, PointReason.ORDER))
                .thenReturn(true);

        assertThrows(OrderAlreadyRewardedException.class,
                () -> validator.validateOrderEarnNotDuplicated(10L));
    }

    // 4. 리뷰 중복 적립
    @Test
    void validateReviewNotDuplicated_noPositiveEarn_success() {
        PointHistory h1 = new PointHistory();
        h1.setPointHistoryChange(0);

        PointHistory h2 = new PointHistory();
        h2.setPointHistoryChange(-10);

        when(repository.findByReviewId(50L))
                .thenReturn(List.of(h1, h2));

        assertDoesNotThrow(() -> validator.validateReviewNotDuplicated(50L));
    }

    @Test
    void validateReviewNotDuplicated_nullId_return() {
        assertDoesNotThrow(() -> validator.validateReviewNotDuplicated(null));
    }

    @Test
    void validateReviewNotDuplicated_throw() {
        PointHistory h = new PointHistory();
        h.setPointHistoryChange(10);

        when(repository.findByReviewId(50L))
                .thenReturn(List.of(h));

        assertThrows(ReviewAlreadyRewardedException.class,
                () -> validator.validateReviewNotDuplicated(50L));
    }

    // 5. 반품 중복 처리
    @Test
    void validateReturnNotDuplicated_throw() {
        when(repository.findByReturnId(77L))
                .thenReturn(List.of(new PointHistory()));

        assertThrows(ReturnAlreadyProcessedException.class,
                () -> validator.validateReturnNotDuplicated(77L));
    }

    @Test
    void validateReturnNotDuplicated_nullId_return() {
        assertDoesNotThrow(() -> validator.validateReturnNotDuplicated(null));
    }

    @Test
    void validateReturnNotDuplicated_empty_success() {
        when(repository.findByReturnId(77L))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> validator.validateReturnNotDuplicated(77L));
    }

    // 6. 만료 row 조회
    @Test
    void getExpiredEarnRows_success() {
        when(repository.findByUserUserIdAndPointExpiredDateBeforeAndRemainingPointGreaterThan(
                eq(1L), any(LocalDateTime.class), eq(0)
        )).thenReturn(List.of(new PointHistory()));

        List<PointHistory> rows = validator.getExpiredEarnRows(1L);

        assertEquals(1, rows.size());
    }

    // 7. USE 중복 체크
    @Test
    void validateUseNotDuplicated_throw() {
        when(repository.existsByOrderIdAndPointReason(99L, PointReason.USE))
                .thenReturn(true);

        assertThrows(PointAlreadyUsedForOrderException.class,
                () -> validator.validateUseNotDuplicated(99L));
    }

    @Test
    void validateUseNotDuplicated_nullOrder_return() {
        assertDoesNotThrow(() -> validator.validateUseNotDuplicated(null));
    }

    @Test
    void validateUseNotDuplicated_success() {
        when(repository.existsByOrderIdAndPointReason(99L, PointReason.USE))
                .thenReturn(false);

        assertDoesNotThrow(() -> validator.validateUseNotDuplicated(99L));
    }

    @Test
    void getAllRemainingEarnRows_success() {
        when(repository.findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThan(1L, 0, 0))
                .thenReturn(List.of(new PointHistory(), new PointHistory()));

        List<PointHistory> rows = validator.getAllRemainingEarnRows(1L);

        assertEquals(2, rows.size());
    }

}
