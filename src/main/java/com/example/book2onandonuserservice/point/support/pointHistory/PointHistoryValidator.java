package com.example.book2onandonuserservice.point.support.pointHistory;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.OrderAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.PointAlreadyUsedForOrderException;
import com.example.book2onandonuserservice.point.exception.PointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.ReturnAlreadyProcessedException;
import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointHistoryValidator {

    private final PointHistoryRepository pointHistoryRepository;

    // 1. 양수 금액 검증
    public void validatePositiveAmount(int amount, String message) {
        if (amount <= 0) {
            throw new IllegalArgumentException(message); // 단순 값 검증이므로 customException 필요 x
        }
    }

    // 2. 사용 가능 범위 검증 (ex. 주문금액-쿠폰 이하인지 등)
    public void validatePointRange(int useAmount, int maxUseAmount) {
        if (useAmount > maxUseAmount) {
            throw new PointRangeExceededException(maxUseAmount);
        }
    }

    // 3. 주문 중복 적립 방지
    public void validateOrderEarnNotDuplicated(Long orderId) {
        boolean exists = pointHistoryRepository.existsByOrderIdAndPointReason(orderId, PointReason.ORDER);
        if (exists) {
            throw new OrderAlreadyRewardedException(orderId);
        }
    }

    // 4. 리뷰 중복 적립 방지
    public void validateReviewNotDuplicated(Long reviewId) {
        if (reviewId == null) {
            return;
        }
        List<PointHistory> histories = pointHistoryRepository.findByReviewId(reviewId);
        boolean exists = histories.stream()
                .anyMatch(h -> h.getPointHistoryChange() > 0);
        if (exists) {
            throw new ReviewAlreadyRewardedException(reviewId);
        }
    }

    // 5. 반품 중복 처리 방지
    public void validateReturnNotDuplicated(Long returnId) {
        if (returnId == null) {
            return;
        }
        List<PointHistory> histories = pointHistoryRepository.findByReturnId(returnId);
        if (!histories.isEmpty()) {
            throw new ReturnAlreadyProcessedException(returnId);
        }
    }

    // 6. 만료 대상 row 조회
    public List<PointHistory> getExpiredEarnRows(Long userId) {
        return pointHistoryRepository
                .findByUserUserIdAndPointExpiredDateBeforeAndRemainingPointGreaterThan(
                        userId,
                        LocalDateTime.now(),
                        0
                );
    }

    // 7. 포인트 차감(USE) 중복 방지 (결제가 성공했을 때 딱 한 번만 실행되어야 하므로 같은 주문에서 USE 두 번 금지)
    public void validateUseNotDuplicated(Long orderId) {
        if (orderId == null) {
            return;
        }
        boolean exists = pointHistoryRepository
                .existsByOrderIdAndPointReason(orderId, PointReason.USE);
        if (exists) {
            throw new PointAlreadyUsedForOrderException(orderId);
        }
    }

    // 8. 회원탈퇴 시 포인트 0으로 강제처리
    public List<PointHistory> getAllRemainingEarnRows(Long userId) {
        return pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThan(
                        userId, 0, 0
                );
    }
}
