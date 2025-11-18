package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.PointReason;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointHistoryServiceImpl implements PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // 1. 포인트 전체 내역 조회
    @Transactional(readOnly = true)
    @Override
    public Page<PointHistoryResponseDto> getMyPointHistory(Long userId, Pageable pageable) {
        Page<PointHistory> page = pointHistoryRepository.findAllByUserUserId(userId, pageable);
        return page.map(this::toDto);
    }

    // 2. 현재 보유 포인트 (최신 1건 기준)
    @Transactional(readOnly = true)
    @Override
    public PointHistoryResponseDto getMyCurrentPoint(Long userId) {
        PointHistory latest = pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 이력이 없습니다. userId=" + userId));
        return toDto(latest);
    }

    // 3. 포인트 적립
    // 3-1. 회원가입 포인트 적립 (고정 5000)
    @Override
    public void earnSignupPoint(Long userId) {
        Users userRef = entityManager.getReference(Users.class, userId);
        earnPointInternal(userId, userRef, PointReason.SIGNUP, null, null, null, null);
    }

    // 3-2. 리뷰 작성 포인트 적립 (고정 500)
    @Override
    public void earnReviewPoint(Long userId, Long reviewId) {
        Users userRef = entityManager.getReference(Users.class, userId);
        earnPointInternal(userId, userRef, PointReason.REVIEW, null, null, reviewId, null);
    }

    // 3-3. 도서 결제 포인트 적립 (적립률)
    @Override
    public void earnOrderPoint(Long userId, Long orderItemId, int orderAmount) {
        Users userRef = entityManager.getReference(Users.class, userId);
        earnPointInternal(userId, userRef, PointReason.ORDER, orderAmount, orderItemId, null, null);
    }

    // 4. 포인트 사용
    @Override
    public void usePoint(Long userId, Long orderItemId, int useAmount) {
        if (useAmount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }
        int latestTotal = pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
        if (latestTotal < useAmount) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재=" + latestTotal + ", 요청=" + useAmount);
        }

        Users userRef = entityManager.getReference(Users.class, userId);
        int newTotal = latestTotal - useAmount;
        PointHistory history = PointHistory.builder()
                .pointHistoryChange(-useAmount)
                .totalPoints(newTotal)
                .pointHistoryReason(PointReason.USE.name())
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(LocalDateTime.now())
                .user(userRef)
                .orderItemId(orderItemId)
                .build();
        pointHistoryRepository.save(history);
    }

    // 5. 포인트 반환 (주문 취소/반품)
    @Override
    public void refundPoint(Long userId, Long orderItemId) {
        List<PointHistory> histories = pointHistoryRepository.findByOrderItemId(orderItemId);
        if (histories.isEmpty()) {
            return; // 이 주문에서 포인트를 건드린 적이 없으면 반환할 것도 없음
        }

        // 이 주문으로 인한 포인트 변화 합계 (예: +1000 적립, -3000 사용 → 합계 -2000)
        int delta = histories.stream()
                .mapToInt(PointHistory::getPointHistoryChange)
                .sum();
        if (delta == 0) {
            return;
        }

        int latestTotal = pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
        // 합계를 뒤집어서 상쇄하는 방향으로 기록
        int change = -delta;                 // delta가 +면 -, delta가 -면 +
        int newTotal = latestTotal + change; // 최종 잔액

        Users userRef = entityManager.getReference(Users.class, userId);

        PointHistory history = PointHistory.builder()
                .pointHistoryChange(change)
                .totalPoints(newTotal)
                .pointHistoryReason(PointReason.REFUND.name())   // "REFUND"
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(LocalDateTime.now())
                .user(userRef)
                .orderItemId(orderItemId)
                .build();

        pointHistoryRepository.save(history);
    }

    // 6. 포인트 만료 처리 (단순: 현재 잔액 전체 소멸)
    @Override
    public void expirePoints(Long userId) {
        int latestTotal = pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);

        if (latestTotal <= 0) {
            return; // 잔액이 없으면 소멸할 것도 없음
        }

        Users userRef = entityManager.getReference(Users.class, userId);

        PointHistory history = PointHistory.builder()
                .pointHistoryChange(-latestTotal)               // 전액 소멸
                .totalPoints(0)
                .pointHistoryReason(PointReason.EXPIRE.name())  // "EXPIRE"
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(LocalDateTime.now())
                .user(userRef)
                .build();

        pointHistoryRepository.save(history);
    }

    // (참고) Users 프록시 가져오는 헬퍼
    private Users getUserRef(Long userId) {
        return entityManager.getReference(Users.class, userId);
    }

    // 내부 공통 적립 로직
    private void earnPointInternal(Long userId,
                                   Users userRef,
                                   PointReason reason,
                                   Integer baseAmount,
                                   Long orderItemId,
                                   Long reviewId,
                                   Long returnId) {

        // 1) 정책 조회 (policyName = SIGNUP / REVIEW / ORDER)
        PointPolicy policy = pointPolicyRepository.findByPolicyName(reason.name())
                .orElseThrow(() -> new IllegalStateException("포인트 정책이 정의되지 않았습니다. reason=" + reason));

        // 2) 이번에 적립할 포인트 계산
        int changePoint = calculatePoint(policy, baseAmount);

        // 3) 현재 총 포인트 가져오기 (이력이 없으면 0)
        int latestTotal = pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);

        int newTotal = latestTotal + changePoint;

        // 4) PointHistory 엔티티 저장
        PointHistory history = PointHistory.builder()
                .pointHistoryChange(changePoint)
                .totalPoints(newTotal)
                .pointHistoryReason(reason.name())                // SIGNUP / REVIEW / ORDER 등
                .pointCreatedDate(LocalDateTime.now())
                .pointExpiredDate(LocalDateTime.now().plusYears(1)) // 예: 1년 뒤 만료
                .user(userRef)                                    // Users 엔티티 프록시
                .orderItemId(orderItemId)
                .reviewId(reviewId)
                .returnEntity(returnId)
                .build();

        pointHistoryRepository.save(history);
    }

    // 정책에 따른 포인트 계산: 고정 / 적립률
    private int calculatePoint(PointPolicy policy, Integer baseAmount) {
        if (policy.getAddPoint() != null) {            // 고정 포인트
            return policy.getAddPoint();
        }
        if (policy.getAddRate() != null) {             // 적립률
            if (baseAmount == null) {
                throw new IllegalArgumentException("적립률 정책에는 baseAmount(주문 금액)가 필요합니다.");
            }
            return (int) Math.round(baseAmount * policy.getAddRate());
        }
        throw new IllegalStateException("포인트 정책 설정이 잘못되었습니다. id=" + policy.getPolicyId());
    }

    // Entity → DTO 변환
    private PointHistoryResponseDto toDto(PointHistory entity) {
        return PointHistoryResponseDto.builder()
                .pointHistoryId(entity.getPointHistoryId())
                .pointHistoryChange(entity.getPointHistoryChange())
                .totalPoints(entity.getTotalPoints())
                .pointHistoryReason(entity.getPointHistoryReason())
                .pointCreatedDate(entity.getPointCreatedDate())
                .pointExpiredDate(entity.getPointExpiredDate())
                .orderItemId(entity.getOrderItemId())
                .reviewId(entity.getReviewId())
                .build();
    }
}
