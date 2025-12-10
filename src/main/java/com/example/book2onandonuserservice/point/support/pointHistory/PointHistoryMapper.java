package com.example.book2onandonuserservice.point.support.pointHistory;

import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class PointHistoryMapper {

    // 엔티티 -> DTO
    // PointPolicy을 DTO로 바꾸는 정적 팩토리 메서드
    public PointHistoryResponseDto toDto(PointHistory entity) {
        return PointHistoryResponseDto.builder()
                .pointHistoryId(entity.getPointHistoryId())
                .pointHistoryChange(entity.getPointHistoryChange())
                .totalPoints(entity.getTotalPoints())
                .pointCreatedDate(entity.getPointCreatedDate())
                .pointExpiredDate(entity.getPointExpiredDate())
                .remainingPoint(entity.getRemainingPoint())
                .pointReason(entity.getPointReason())
                .orderId(entity.getOrderId())
                .reviewId(entity.getReviewId())
                .returnId(entity.getReturnId())
                .build();
    }

    // 적립 row 생성 (SIGNUP / REVIEW / ORDER / REFUND 등 + 방향)
    public PointHistory toEarnEntity(
            Users user,
            PointReason reason,
            int change,
            int currentPoint,
            Long orderId,
            Long reviewId,
            Long returnId,
            LocalDateTime expiredAt
    ) {
        int newTotal = currentPoint + change;

        return PointHistory.builder()
                .user(user)
                .pointReason(reason)
                .pointHistoryChange(change)
                .totalPoints(newTotal)
                .remainingPoint(change) // 적립된 포인트만큼 remaining 시작
                .orderId(orderId)
                .reviewId(reviewId)
                .returnId(returnId)
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(expiredAt)
                .build();
    }

    // 차감 row 생성 (USE / REFUND 회수 / EXPIRE 등)
    public PointHistory toUseOrDeductEntity(
            Users user,
            PointReason reason,
            int change,       // 음수
            int currentPoint,
            Long orderId,
            Long reviewId,
            Long returnId
    ) {
        int newTotal = currentPoint + change;

        return PointHistory.builder()
                .user(user)
                .pointReason(reason)
                .pointHistoryChange(change)
                .totalPoints(newTotal)
                .remainingPoint(null)  // 차감 row는 remaining 없음
                .orderId(orderId)
                .reviewId(reviewId)
                .returnId(returnId)
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(LocalDateTime.now()) // 의미 없는 값이지만 not null 대응
                .build();
    }
}
