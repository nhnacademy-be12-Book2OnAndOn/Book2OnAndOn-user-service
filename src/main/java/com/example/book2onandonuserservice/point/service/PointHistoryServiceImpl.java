package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.domain.entity.PointReviewType;
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;

import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.support.pointHistory.PointCalculationHelper;
import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryMapper;
import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryValidator;
import com.example.book2onandonuserservice.point.support.pointHistory.UserReferenceLoader;
import com.example.book2onandonuserservice.user.domain.entity.Users;
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
    private final UserReferenceLoader userReferenceLoader;
    private final PointCalculationHelper pointCalculationHelper;
    private final PointHistoryMapper pointHistoryMapper;
    private final PointHistoryValidator pointHistoryValidator;

    // ===== 공통 유틸 =====
    // 1. 현재 보유 포인트 숫자만 필요할 때 사용하는 내부 헬퍼
    private int getLatestTotal(Long userId) {
        return pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
    }

    // 2. 포인트 적립 시 만료일 계산 (기본 1년 뒤)
    private LocalDateTime getDefaultExpireAt() {
        return LocalDateTime.now().plusYears(1);
//        return LocalDateTime.now().minusDays(1); // 만료일자 지난 포인트 처리 테스트용
    }

    // ===== User =====
    // 1. 포인트 전체 내역 조회 (마이페이지)
    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponseDto> getMyPointHistory(Long userId, Pageable pageable) {
        Page<PointHistory> page =
                pointHistoryRepository.findAllByUserUserIdOrderByPointCreatedDateDesc(userId, pageable);
        return page.map(pointHistoryMapper::toDto);
    }

    // 2. 현재 보유 포인트 조회
    @Override
    @Transactional(readOnly = true)
    public CurrentPointResponseDto getMyCurrentPoint(Long userId) {
        int latestTotal = getLatestTotal(userId);
        return new CurrentPointResponseDto(latestTotal);
    }

    // 3-1. 회원가입 적립
    @Override
    public EarnPointResponseDto earnSignupPoint(Long userId) {
        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

        // 유저의 회원가입 유무 판별
        boolean existsSignup = pointHistoryRepository.existsByUserUserIdAndPointReason(userId, PointReason.SIGNUP);
        if (existsSignup) {
//            return new EarnPointResponseDto(0, latestTotal, PointReason.SIGNUP); -> 이미 회원가입된 유저일 경우, 0포인트 지급
            throw new SignupPointAlreadyGrantedException(userId); // -> 예외처리
        }
        // 정책이 비활성화거나, 적립포인트 0일 경우 적립 x
        int change = pointCalculationHelper.calculateByReason(PointReason.SIGNUP, null);
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.SIGNUP);
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.SIGNUP,
                change,
                newTotal,
                null, // orderItemId
                null, // reviewId
                null, // returnId
                getDefaultExpireAt()
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.SIGNUP);
    }

    // 3-2. 리뷰 작성 적립 (일반 / 사진)
    @Override
    public EarnPointResponseDto earnReviewPoint(EarnReviewPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long reviewId = dto.getReviewId();
        Long orderItemId = dto.getOrderItemId();
        PointReviewType reviewType = dto.getReviewType();

        // 1) 리뷰 중복 적립 방지
        pointHistoryValidator.validateReviewNotDuplicated(reviewId);

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

        // 2) 리뷰 타입에 따른 정책명 결정
        String policyName = switch (reviewType) {
            case PHOTO -> "REVIEW_PHOTO";
            case TEXT -> "REVIEW_TEXT";
        };

        int change = pointCalculationHelper.calculateByPolicyName(policyName, null);
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.REVIEW);
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.REVIEW,
                change,
                newTotal,
                orderItemId,
                reviewId,
                null,
                getDefaultExpireAt()
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.REVIEW);
    }

    // 3-3. 도서 결제 적립 (기본 적립률)
    @Override
    public EarnPointResponseDto earnOrderPoint(EarnOrderPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderItemId = dto.getOrderItemId();
        int orderAmount = dto.getOrderAmount();

        pointHistoryValidator.validateOrderEarnNotDuplicated(orderItemId);
        pointHistoryValidator.validatePositiveAmount(orderAmount, "주문 금액은 0보다 커야 합니다.");

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

        // 정책명: ORDER (결제 금액 × 기본 적립률)
        int change = pointCalculationHelper.calculateByReason(PointReason.ORDER, orderAmount);
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.ORDER);
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.ORDER,
                change,
                newTotal,
                orderItemId,
                null,
                null,
                getDefaultExpireAt()
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.ORDER);
    }

    // 3-4. 등급 적립 (내부용)
    @Override
    public void earnGradePoint(Long userId, int pureAmount, double gradeRewardRate) {
        pointHistoryValidator.validatePositiveAmount(pureAmount, "순수금액은 0보다 커야 합니다.");
        if (gradeRewardRate <= 0) {
            return;
        }

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

        int change = (int) Math.round(pureAmount * gradeRewardRate);
        if (change <= 0) {
            return;
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.ORDER,   // 등급 적립도 주문 관련 적립으로 보는 구조
                change,
                newTotal,
                null,
                null,
                null,
                getDefaultExpireAt()
        );
        pointHistoryRepository.save(history);
    }

    // 4. 포인트 사용
    @Override
    public EarnPointResponseDto usePoint(UsePointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderItemId = dto.getOrderItemId();
        int useAmount = dto.getUseAmount();
        int allowedMaxUseAmount = dto.getAllowedMaxUseAmount();

        // 1) 주문에서 허용한 최대 사용 가능 포인트 범위 검증
        pointHistoryValidator.validatePointRange(useAmount, allowedMaxUseAmount);

        // 2) 양수 검증
        pointHistoryValidator.validatePositiveAmount(useAmount, "사용 포인트는 0보다 커야 합니다.");

        // 3) 같은 주문에 대한 중복 사용 방지
        pointHistoryValidator.validateUseNotDuplicated(orderItemId);

        int latestTotal = getLatestTotal(userId);
        if (latestTotal < useAmount) {
            throw new InsufficientPointException(latestTotal, useAmount);
        }

        Users user = userReferenceLoader.getReference(userId);

        // 4) FIFO 방식으로 remaining_point 차감
        int remainToUse = useAmount;
        List<PointHistory> earnRows =
                pointHistoryRepository.findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointCreatedDateAsc(
                        userId,
                        0,
                        0
                );

        for (PointHistory row : earnRows) {
            if (remainToUse <= 0) {
                break;
            }
            Integer remaining = row.getRemainingPoint();
            if (remaining == null || remaining <= 0) {
                continue;
            }

            int usedHere = Math.min(remaining, remainToUse);
            row.setRemainingPoint(remaining - usedHere);
            remainToUse -= usedHere;
        }

        if (remainToUse > 0) {
            throw new IllegalStateException("remaining_point 합계가 부족합니다. 데이터 정합성을 확인하세요.");
        }

        // 5) 사용 이력 row 생성
        int newTotal = latestTotal - useAmount;
        PointHistory useHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.USE,
                -useAmount,
                newTotal,
                orderItemId,
                null,
                null
        );
        pointHistoryRepository.save(useHistory);

        return new EarnPointResponseDto(-useAmount, newTotal, PointReason.USE);
    }

    // 5. 포인트 반환 (결제취소/반품)
    @Override
    public EarnPointResponseDto refundPoint(RefundPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderItemId = dto.getOrderItemId();
        Long returnId = dto.getReturnId();
        int usedPoint = dto.getUsedPoint() != null ? dto.getUsedPoint() : 0;
        int returnAmount = dto.getReturnAmount() != null ? dto.getReturnAmount() : 0;

        // 동일 returnId 중복 처리 방지
        pointHistoryValidator.validateReturnNotDuplicated(returnId);

        Users user = userReferenceLoader.getReference(userId);
        int beforeTotal = getLatestTotal(userId);
        int latestTotal = beforeTotal;

        // 5-1. 사용 포인트 복구 (+usedPoint)
        if (usedPoint > 0) {
            pointHistoryValidator.validatePositiveAmount(usedPoint, "복구 포인트는 0보다 커야 합니다.");

            int newTotal = latestTotal + usedPoint;
            PointHistory restoreHistory = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.REFUND,
                    usedPoint,
                    newTotal,
                    orderItemId,
                    null,
                    returnId,
                    getDefaultExpireAt()
            );
            pointHistoryRepository.save(restoreHistory);
            latestTotal = newTotal;
        }

        // 5-2. 반품 금액만큼 포인트 적립 (+returnAmount)
        if (returnAmount > 0) {
            pointHistoryValidator.validatePositiveAmount(returnAmount, "반품 금액은 0보다 커야 합니다.");

            int newTotal = latestTotal + returnAmount;
            PointHistory refundAmountHistory = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.REFUND,
                    returnAmount,
                    newTotal,
                    orderItemId,
                    null,
                    returnId,
                    getDefaultExpireAt()
            );
            pointHistoryRepository.save(refundAmountHistory);
            latestTotal = newTotal;
        }

        // 5-3. 이 주문에 대해 적립되었던 리뷰 포인트 회수
        List<PointHistory> orderHistories = pointHistoryRepository.findByOrderItemId(orderItemId);
        int reviewEarnSum = orderHistories.stream()
                .filter(h -> h.getPointReason() == PointReason.REVIEW && h.getPointHistoryChange() > 0)
                .mapToInt(PointHistory::getPointHistoryChange)
                .sum();

        if (reviewEarnSum > 0) {
            int newTotal = latestTotal - reviewEarnSum;
            PointHistory cancelReviewHistory = pointHistoryMapper.toUseOrDeductEntity(
                    user,
                    PointReason.REFUND,   // 회수도 REFUND 범주로 기록
                    -reviewEarnSum,
                    newTotal,
                    orderItemId,
                    null,
                    returnId
            );
            pointHistoryRepository.save(cancelReviewHistory);
            latestTotal = newTotal;
        }

        int netChange = latestTotal - beforeTotal;
        return new EarnPointResponseDto(netChange, latestTotal, PointReason.REFUND);
    }

    // 6. 포인트 만료 처리
    @Override
    public void expirePoints(Long userId) {
        List<PointHistory> expiredRows = pointHistoryValidator.getExpiredEarnRows(userId);
        if (expiredRows.isEmpty()) {
            return;
        }

        int expireTotal = expiredRows.stream()
                .mapToInt(row -> row.getRemainingPoint() == null ? 0 : row.getRemainingPoint())
                .sum();

        if (expireTotal <= 0) {
            return;
        }

        // 각 row의 remaining_point 를 0으로
        for (PointHistory row : expiredRows) {
            row.setRemainingPoint(0);
        }

        int latestTotal = getLatestTotal(userId);
        int newTotal = latestTotal - expireTotal;

        Users user = userReferenceLoader.getReference(userId);

        PointHistory expireHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.EXPIRE,
                -expireTotal,
                newTotal,
                null,
                null,
                null
        );
        pointHistoryRepository.save(expireHistory);
    }

    // 7. 관리자 수동 포인트 지급/차감
    @Override
    public EarnPointResponseDto adjustPointByAdmin(PointHistoryAdminAdjustRequestDto requestDto) {
        Long userId = requestDto.getUserId();
        int amount = requestDto.getAmount();

        if (amount == 0) {
            throw new IllegalArgumentException("조정 포인트는 0일 수 없습니다.");
        }

        pointHistoryValidator.validatePositiveAmount(Math.abs(amount), "조정 포인트는 0보다 커야 합니다.");

        int latestTotal = getLatestTotal(userId);
        int newTotal = latestTotal + amount;
        if (newTotal < 0) {
            throw new IllegalStateException("조정 후 포인트가 음수가 될 수 없습니다.");
        }

        Users user = userReferenceLoader.getReference(userId);

        PointHistory history;
        if (amount > 0) {
            history = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.ADMIN_ADJUST,
                    amount,
                    newTotal,
                    null,
                    null,
                    null,
                    getDefaultExpireAt()
            );
        } else {
            history = pointHistoryMapper.toUseOrDeductEntity(
                    user,
                    PointReason.ADMIN_ADJUST,
                    amount,
                    newTotal,
                    null,
                    null,
                    null
            );
        }
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(amount, newTotal, PointReason.ADMIN_ADJUST);
    }
}