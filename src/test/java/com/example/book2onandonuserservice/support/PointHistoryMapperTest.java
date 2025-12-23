package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryMapper;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PointHistoryMapperTest {

    private final PointHistoryMapper mapper = new PointHistoryMapper();

    @Test
    @DisplayName("toDto: 엔티티 필드가 DTO로 정확히 매핑")
    void toDto_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        LocalDateTime created = LocalDateTime.of(2025, 12, 13, 10, 0);
        LocalDateTime expired = LocalDateTime.of(2025, 12, 20, 10, 0);

        PointHistory entity = PointHistory.builder()
                .user(user)
                .pointHistoryId(999L)
                .pointHistoryChange(50)
                .totalPoints(100)
                .remainingPoint(50)
                .pointReason(PointReason.REVIEW)
                .orderId(10L)
                .reviewId(20L)
                .refundId(30L)
                .pointCreatedDate(created)
                .pointExpiredDate(expired)
                .build();

        PointHistoryResponseDto dto = mapper.toDto(entity);

        assertAll(
                () -> assertEquals(entity.getPointHistoryId(), dto.getPointHistoryId()),
                () -> assertEquals(entity.getPointHistoryChange(), dto.getPointHistoryChange()),
                () -> assertEquals(entity.getTotalPoints(), dto.getTotalPoints()),
                () -> assertEquals(entity.getRemainingPoint(), dto.getRemainingPoint()),
                () -> assertEquals(entity.getPointReason(), dto.getPointReason()),
                () -> assertEquals(entity.getOrderId(), dto.getOrderId()),
                () -> assertEquals(entity.getReviewId(), dto.getReviewId()),
                () -> assertEquals(entity.getRefundId(), dto.getRefundId()),
                () -> assertEquals(entity.getPointCreatedDate(), dto.getPointCreatedDate()),
                () -> assertEquals(entity.getPointExpiredDate(), dto.getPointExpiredDate())
        );
    }

    @Test
    @DisplayName("toEarnEntity: 적립 row 생성 시 remainingPoint=change, expiredAt 반영, createdDate는 now")
    void toEarnEntity_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        LocalDateTime expired = LocalDateTime.now().plusDays(7);

        // now()는 테스트에서 범위로 검증(플래키 방지)
        LocalDateTime before = LocalDateTime.now();

        PointHistory history = mapper.toEarnEntity(
                user,
                PointReason.REVIEW,
                50,
                100,
                10L,
                20L,
                30L,
                expired
        );

        LocalDateTime after = LocalDateTime.now();

        assertAll(
                () -> assertEquals(50, history.getPointHistoryChange()),
                () -> assertEquals(100, history.getTotalPoints()),
                () -> assertEquals(50, history.getRemainingPoint()),
                () -> assertEquals(PointReason.REVIEW, history.getPointReason()),
                () -> assertEquals(10L, history.getOrderId()),
                () -> assertEquals(20L, history.getReviewId()),
                () -> assertEquals(30L, history.getRefundId()),
                () -> assertEquals(expired, history.getPointExpiredDate()),

                // createdDate는 [before, after] 범위 안이면 OK
                () -> assertNotNull(history.getPointCreatedDate()),
                () -> assertFalse(history.getPointCreatedDate().isBefore(before)),
                () -> assertFalse(history.getPointCreatedDate().isAfter(after))
        );
    }

    @Test
    @DisplayName("toUseOrDeductEntity: 차감 row 생성 시 remainingPoint=null, expiredDate=null, createdDate는 now")
    void toUseOrDeductEntity_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        LocalDateTime before = LocalDateTime.now();

        PointHistory history = mapper.toUseOrDeductEntity(
                user,
                PointReason.USE,
                -30,
                70,
                10L,
                null,
                null
        );

        LocalDateTime after = LocalDateTime.now();

        assertAll(
                () -> assertEquals(-30, history.getPointHistoryChange()),
                () -> assertEquals(70, history.getTotalPoints()),
                () -> assertNull(history.getRemainingPoint()),
                () -> assertEquals(PointReason.USE, history.getPointReason()),
                () -> assertEquals(10L, history.getOrderId()),
                () -> assertNull(history.getReviewId()),
                () -> assertNull(history.getRefundId()),
                () -> assertNull(history.getPointExpiredDate()),

                () -> assertNotNull(history.getPointCreatedDate()),
                () -> assertFalse(history.getPointCreatedDate().isBefore(before)),
                () -> assertFalse(history.getPointCreatedDate().isAfter(after))
        );
    }
}
