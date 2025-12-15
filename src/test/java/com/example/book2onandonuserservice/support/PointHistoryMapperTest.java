package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryMapper;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PointHistoryMapperTest {

    PointHistoryMapper mapper = new PointHistoryMapper();

    @Test
    void toDto_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        LocalDateTime created = LocalDateTime.of(2025, 12, 13, 10, 0);
        LocalDateTime expired = LocalDateTime.of(2025, 12, 20, 10, 0);

        PointHistory entity = PointHistory.builder()
                .user(user)
                .pointHistoryChange(50)
                .totalPoints(100)
                .remainingPoint(50)
                .pointReason(PointReason.REVIEW)
                .orderId(10L)
                .reviewId(20L)
                .returnId(30L)
                .pointCreatedDate(created)
                .pointExpiredDate(expired)
                .build();

        PointHistoryResponseDto dto = mapper.toDto(entity);

        assertEquals(entity.getPointHistoryChange(), dto.getPointHistoryChange());
        assertEquals(entity.getTotalPoints(), dto.getTotalPoints());
        assertEquals(entity.getRemainingPoint(), dto.getRemainingPoint());
        assertEquals(entity.getPointReason(), dto.getPointReason());
        assertEquals(entity.getOrderId(), dto.getOrderId());
        assertEquals(entity.getReviewId(), dto.getReviewId());
        assertEquals(entity.getReturnId(), dto.getReturnId());
        assertEquals(entity.getPointCreatedDate(), dto.getPointCreatedDate());
        assertEquals(entity.getPointExpiredDate(), dto.getPointExpiredDate());
    }

    @Test
    void toEarnEntity_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        LocalDateTime expired = LocalDateTime.now().plusDays(7);

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

        assertEquals(50, history.getPointHistoryChange());
        assertEquals(100, history.getTotalPoints());
        assertEquals(50, history.getRemainingPoint());
        assertEquals(PointReason.REVIEW, history.getPointReason());
        assertEquals(10L, history.getOrderId());
        assertEquals(20L, history.getReviewId());
        assertEquals(30L, history.getReturnId());
        assertEquals(expired, history.getPointExpiredDate());
    }

    @Test
    void toUseOrDeductEntity_success() {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);

        PointHistory history = mapper.toUseOrDeductEntity(
                user,
                PointReason.USE,
                -30,
                70,
                10L,
                null,
                null
        );

        assertEquals(-30, history.getPointHistoryChange());
        assertEquals(70, history.getTotalPoints());
        assertNull(history.getRemainingPoint());
        assertEquals(PointReason.USE, history.getPointReason());
        assertEquals(10L, history.getOrderId());
    }
}
