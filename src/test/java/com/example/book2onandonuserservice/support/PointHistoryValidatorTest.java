//package com.example.book2onandonuserservice.support;
//
//import static org.junit.jupiter.api.Assertions.assertAll;
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
//import com.example.book2onandonuserservice.point.domain.entity.PointReason;
//import com.example.book2onandonuserservice.point.exception.InvalidAdminAdjustPointException;
//import com.example.book2onandonuserservice.point.exception.OrderAlreadyRewardedException;
//import com.example.book2onandonuserservice.point.exception.PointAlreadyUsedForOrderException;
//import com.example.book2onandonuserservice.point.exception.PointRangeExceededException;
//import com.example.book2onandonuserservice.point.exception.ReturnAlreadyProcessedException;
//import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
//import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
//import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryValidator;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class PointHistoryValidatorTest {
//
//    @Mock
//    PointHistoryRepository repository;
//
//    @InjectMocks
//    PointHistoryValidator validator;
//
//    // 1. 양수 금액 검증
//    @Test
//    @DisplayName("validatePositiveAmount: amount가 1 이상이면 통과")
//    void validatePositiveAmount_success() {
//        assertDoesNotThrow(() -> validator.validatePositiveAmount(10, "msg"));
//    }
//
//    @Test
//    @DisplayName("validatePositiveAmount: amount가 0 이하이면 IllegalArgumentException")
//    void validatePositiveAmount_fail() {
//        IllegalArgumentException ex = assertThrows(
//                IllegalArgumentException.class,
//                () -> validator.validatePositiveAmount(0, "msg")
//        );
//        assertEquals("msg", ex.getMessage());
//    }
//
//    // 2. 사용 가능 범위 검증
//    @Test
//    @DisplayName("validatePointRange: useAmount <= maxUseAmount면 통과")
//    void validatePointRange_success() {
//        assertAll(
//                () -> assertDoesNotThrow(() -> validator.validatePointRange(100, 100)),
//                () -> assertDoesNotThrow(() -> validator.validatePointRange(50, 100))
//        );
//    }
//
//    @Test
//    @DisplayName("validatePointRange: useAmount > maxUseAmount면 PointRangeExceededException")
//    void validatePointRange_fail() {
//        assertThrows(PointRangeExceededException.class,
//                () -> validator.validatePointRange(200, 100));
//    }
//
//    // 3. 주문 중복 적립 방지
//    @Test
//    @DisplayName("validateOrderEarnNotDuplicated: ORDER 내역이 없으면 통과")
//    void validateOrderEarnNotDuplicated_success() {
//        when(repository.existsByOrderIdAndPointReason(10L, PointReason.ORDER))
//                .thenReturn(false);
//
//        assertDoesNotThrow(() -> validator.validateOrderEarnNotDuplicated(10L));
//        verify(repository).existsByOrderIdAndPointReason(10L, PointReason.ORDER);
//    }
//
//    @Test
//    @DisplayName("validateOrderEarnNotDuplicated: ORDER 내역이 이미 있으면 OrderAlreadyRewardedException")
//    void validateOrderEarnNotDuplicated_throw() {
//        when(repository.existsByOrderIdAndPointReason(10L, PointReason.ORDER))
//                .thenReturn(true);
//
//        assertThrows(OrderAlreadyRewardedException.class,
//                () -> validator.validateOrderEarnNotDuplicated(10L));
//        verify(repository).existsByOrderIdAndPointReason(10L, PointReason.ORDER);
//    }
//
//    // 4. 리뷰 중복 적립 방지
//    @Test
//    @DisplayName("validateReviewNotDuplicated: reviewId가 null이면 바로 return")
//    void validateReviewNotDuplicated_nullId_return() {
//        assertDoesNotThrow(() -> validator.validateReviewNotDuplicated(null));
//        verifyNoInteractions(repository);
//    }
//
//    @Test
//    @DisplayName("validateReviewNotDuplicated: 양수 적립 내역이 없으면 통과")
//    void validateReviewNotDuplicated_noPositiveEarn_success() {
//        PointHistory h1 = mock(PointHistory.class);
//        when(h1.getPointHistoryChange()).thenReturn(0);
//
//        PointHistory h2 = mock(PointHistory.class);
//        when(h2.getPointHistoryChange()).thenReturn(-10);
//
//        when(repository.findByReviewId(50L))
//                .thenReturn(List.of(h1, h2));
//
//        assertDoesNotThrow(() -> validator.validateReviewNotDuplicated(50L));
//        verify(repository).findByReviewId(50L);
//    }
//
//    @Test
//    @DisplayName("validateReviewNotDuplicated: 양수 적립 내역이 있으면 ReviewAlreadyRewardedException")
//    void validateReviewNotDuplicated_throw() {
//        PointHistory h = mock(PointHistory.class);
//        when(h.getPointHistoryChange()).thenReturn(10);
//
//        when(repository.findByReviewId(50L))
//                .thenReturn(List.of(h));
//
//        assertThrows(ReviewAlreadyRewardedException.class,
//                () -> validator.validateReviewNotDuplicated(50L));
//        verify(repository).findByReviewId(50L);
//    }
//
//    // 5. 반품 중복 처리 방지
//    @Test
//    @DisplayName("validateReturnNotDuplicated: refundId가 null이면 바로 return")
//    void validateReturnNotDuplicated_nullId_return() {
//        assertDoesNotThrow(() -> validator.validateReturnNotDuplicated(null));
//        verifyNoInteractions(repository);
//    }
//
//    @Test
//    @DisplayName("validateReturnNotDuplicated: refundId로 조회 결과가 비어있으면 통과")
//    void validateReturnNotDuplicated_empty_success() {
//        when(repository.findByRefundId(77L))
//                .thenReturn(Collections.emptyList());
//
//        assertDoesNotThrow(() -> validator.validateReturnNotDuplicated(77L));
//        verify(repository).findByRefundId(77L);
//    }
//
//    @Test
//    @DisplayName("validateReturnNotDuplicated: refundId로 조회 결과가 있으면 ReturnAlreadyProcessedException")
//    void validateReturnNotDuplicated_throw() {
//        when(repository.findByRefundId(77L))
//                .thenReturn(List.of(mock(PointHistory.class)));
//
//        assertThrows(ReturnAlreadyProcessedException.class,
//                () -> validator.validateReturnNotDuplicated(77L));
//        verify(repository).findByRefundId(77L);
//    }
//
//    // 6. 만료 대상 row 조회
//    @Test
//    @DisplayName("getExpiredEarnRows: repository에서 만료 row를 조회해 반환")
//    void getExpiredEarnRows_success() {
//        when(repository.findAlreadyExpiredPoints(eq(1L), any(LocalDateTime.class)))
//                .thenReturn(List.of(mock(PointHistory.class)));
//
//        List<PointHistory> rows = validator.getExpiredEarnRows(1L);
//
//        assertEquals(1, rows.size());
//        verify(repository).findAlreadyExpiredPoints(eq(1L), any(LocalDateTime.class));
//    }
//
//    // 7. USE 중복 방지
//    @Test
//    @DisplayName("validateUseNotDuplicated: orderId가 null이면 바로 return")
//    void validateUseNotDuplicated_nullOrder_return() {
//        assertDoesNotThrow(() -> validator.validateUseNotDuplicated(null));
//        verifyNoInteractions(repository);
//    }
//
//    @Test
//    @DisplayName("validateUseNotDuplicated: USE 내역이 없으면 통과")
//    void validateUseNotDuplicated_success() {
//        when(repository.existsByOrderIdAndPointReason(99L, PointReason.USE))
//                .thenReturn(false);
//
//        assertDoesNotThrow(() -> validator.validateUseNotDuplicated(99L));
//        verify(repository).existsByOrderIdAndPointReason(99L, PointReason.USE);
//    }
//
//    @Test
//    @DisplayName("validateUseNotDuplicated: USE 내역이 이미 있으면 PointAlreadyUsedForOrderException")
//    void validateUseNotDuplicated_throw() {
//        when(repository.existsByOrderIdAndPointReason(99L, PointReason.USE))
//                .thenReturn(true);
//
//        assertThrows(PointAlreadyUsedForOrderException.class,
//                () -> validator.validateUseNotDuplicated(99L));
//        verify(repository).existsByOrderIdAndPointReason(99L, PointReason.USE);
//    }
//
//    // 8. 회원탈퇴 시 잔여 포인트 row 조회
//    @Test
//    @DisplayName("getAllRemainingEarnRows: remaining point row 목록을 반환")
//    void getAllRemainingEarnRows_success() {
//        when(repository.findAllRemianingPoints(1L))
//                .thenReturn(List.of(mock(PointHistory.class), mock(PointHistory.class)));
//
//        List<PointHistory> rows = validator.getAllRemainingEarnRows(1L);
//
//        assertEquals(2, rows.size());
//        verify(repository).findAllRemianingPoints(1L);
//    }
//
//    // 9. 관리자 조정 포인트 0 금지
//    @Test
//    @DisplayName("validateAdjustAmountNotZero: amount가 0이면 InvalidAdminAdjustPointException")
//    void validateAdjustAmountNotZero_throw() {
//        assertThrows(InvalidAdminAdjustPointException.class,
//                () -> validator.validateAdjustAmountNotZero(0));
//    }
//
//    @Test
//    @DisplayName("validateAdjustAmountNotZero: amount가 0이 아니면 통과")
//    void validateAdjustAmountNotZero_success() {
//        assertDoesNotThrow(() -> validator.validateAdjustAmountNotZero(1));
//        assertDoesNotThrow(() -> validator.validateAdjustAmountNotZero(-1));
//    }
//}
