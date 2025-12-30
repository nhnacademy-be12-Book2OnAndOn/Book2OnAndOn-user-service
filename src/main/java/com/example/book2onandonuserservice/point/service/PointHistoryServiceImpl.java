package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointSummaryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.AdminAdjustPointNegativeBalanceException;
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.InvalidPointRateException;
import com.example.book2onandonuserservice.point.exception.InvalidRefundPointException;
import com.example.book2onandonuserservice.point.exception.PointBalanceIntegrityException;
import com.example.book2onandonuserservice.point.exception.RefundPointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.support.pointhistory.PointCalculationHelper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryMapper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryValidator;
import com.example.book2onandonuserservice.point.support.pointhistory.UserReferenceLoader;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.MissingRequiredFieldException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
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
    private final UsersRepository usersRepository;
    private final UserReferenceLoader userReferenceLoader;
    private final PointCalculationHelper pointCalculationHelper;
    private final PointHistoryMapper pointHistoryMapper;
    private final PointHistoryValidator pointHistoryValidator;

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_REVIEW_ID = "reviewId";
    private static final String FIELD_ORDER_ID = "orderId";
    private static final String FIELD_REFUND_ID = "refundId";
    private static final String FIELD_PURE_AMOUNT = "pureAmount";
    private static final String FIELD_USE_AMOUNT = "useAmount";
    private static final String FIELD_ALLOWED_MAX_USE_AMOUNT = "allowedMaxUseAmount";

    // ===== 공통 유틸 =====
    // 1. 현재 보유 포인트 "숫자만" 필요할 때 사용하는 내부 헬퍼
    private int getLatestTotalForRead(Long userId) {
        return pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
    }

    // 포인트 변경 트랜잭션(earn/use/refund/expire/withdraw)은 이걸 사용
    private int getLatestTotalForUpdate(Long userId) {
        return pointHistoryRepository
                .findLatestForUpdateOne(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
    }

    // 2. 포인트 적립 시 만료일 (기본 1년)
    // 2025-01-01 00:00:00.000 ~ 2025-12-31 23:59:59.999999999
    private LocalDateTime getDefaultExpireAt() {
        LocalDate expireDate = LocalDate.now().plusYears(1);
        return expireDate.atStartOfDay().minusNanos(1);
    }

    // 3. 회원가입 적립 전용 만료일 (10일)
    private LocalDateTime getSignupExpireAt() {
        LocalDate expireDate = LocalDate.now().plusDays(10);
        return expireDate.atStartOfDay().minusNanos(1);
    }

    // 4. 취소 환불용 만료일 ㅠㅠ
    private LocalDateTime getCancelRefundExpireAt() {
        LocalDateTime expire = LocalDateTime.now().plusDays(30);
        return expire.toLocalDate().atStartOfDay().minusNanos(1);
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

    // 1-1. 포인트 내역 조회 (필터: 적립/사용)
    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponseDto> getMyPointHistoryByType(Long userId, String type, Pageable pageable) {

        Page<PointHistory> page;
        //GET /api/points/history?type=EARN
        if ("EARN".equalsIgnoreCase(type)) {
            page = pointHistoryRepository
                    .findEarnedPoints(
                            userId, pageable);
        }
        //GET /api/points/history?type=USE
        else if ("USE".equalsIgnoreCase(type)) {
            page = pointHistoryRepository
                    .findUsedPoints(
                            userId, pageable);
        } else {
            page = pointHistoryRepository
                    .findAllByUserUserIdOrderByPointCreatedDateDesc(userId, pageable);
        }

        return page.map(pointHistoryMapper::toDto);
    }

    // 2. 현재 보유 포인트 조회
    @Override
    @Transactional(readOnly = true)
    public CurrentPointResponseDto getMyCurrentPoint(Long userId) {
        int latestTotal = getLatestTotalForRead(userId);
        return new CurrentPointResponseDto(latestTotal);
    }

    // 3. 포인트 적립 (+)
    // 3-1. 회원가입 적립
    @Override
    public EarnPointResponseDto earnSignupPoint(Long userId) {
        // 유저 로딩
        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotalForUpdate(userId);

        // 회원가입으로 포인트를 받은 적이 있는지 여부
        boolean existsSignup = pointHistoryRepository.existsByUserUserIdAndPointReason(userId, PointReason.SIGNUP);
        if (existsSignup) {
            // 택 1
            // return new EarnPointResponseDto(0, latestTotal, PointReason.SIGNUP); // -> 이미 회원가입된 유저일 경우, 0포인트 지급
            throw new SignupPointAlreadyGrantedException(userId); // -> 예외처리
        }

        // 정책에서 고정 포인트 계산 (비활성/0P ➜ 0 반환)
        int change = pointCalculationHelper.calculateByReason(PointReason.SIGNUP);

        // 비활성 or 0P 설정 ➜ 적립 없이 끝 (회원가입은 성공, 포인트는 없음)
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.SIGNUP);
        }

        // 포인트 적립 및 이력 저장
        int newTotal = latestTotal + change;
        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.SIGNUP,
                change, // pointHistoryChange
                newTotal, // totalPoints
                null, // orderId
                null, // reviewId
                null, // refundId
                getSignupExpireAt() // 전용 만료일 처리
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.SIGNUP);
    }

    // 3-2. 리뷰 작성 적립 (일반 / 사진)
    @Override
    public EarnPointResponseDto earnReviewPoint(EarnReviewPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long reviewId = dto.getReviewId();
        boolean hasImage = dto.isHasImage();

        if (userId == null) {
            throw new MissingRequiredFieldException(FIELD_USER_ID);
        }
        if (reviewId == null) {
            throw new MissingRequiredFieldException(FIELD_REVIEW_ID);
        }

        // 1) 리뷰 중복 적립 방지
        pointHistoryValidator.validateReviewNotDuplicated(reviewId);

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotalForUpdate(userId);

        // 2) 리뷰 타입에 따른 정책명 결정 ((일반 리뷰: 200원, request.isHasImage() == true면 500원)
        String policyName = hasImage ? "REVIEW_PHOTO" : "REVIEW_TEXT";

        int change = pointCalculationHelper.calculateByPolicyName(policyName);
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.REVIEW);
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.REVIEW,
                change,
                newTotal,
                null,
                reviewId,
                null,
                getDefaultExpireAt()
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.REVIEW);
    }

    // 3-3. 도서 결제 적립 (유저 등급별 기본 적립률 책정)
    @Override
    public EarnPointResponseDto earnOrderPoint(EarnOrderPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        Integer pureAmountObj = dto.getPureAmount();
        Double gradeRate = dto.getPointAddRate();

        if (userId == null) {
            throw new MissingRequiredFieldException(FIELD_USER_ID);
        }
        if (orderId == null) {
            throw new MissingRequiredFieldException(FIELD_ORDER_ID);
        }
        if (pureAmountObj == null) {
            throw new MissingRequiredFieldException(FIELD_PURE_AMOUNT);
        }
        if (gradeRate == null) {
            throw new InvalidPointRateException(null);
        }

        int pureAmount = pureAmountObj;

        // 1) 주문 중복 적립 방지
        pointHistoryValidator.validateOrderEarnNotDuplicated(orderId);

        // 2) 금액/비율 검증
        pointHistoryValidator.validatePositiveAmount(pureAmount, "주문 금액은 0보다 커야 합니다.");
        if (gradeRate == null || gradeRate <= 0) {
            throw new InvalidPointRateException(gradeRate);
        }

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotalForUpdate(userId);

        // 3) 등급 적립률로 적립 포인트 계산
        int change = (int) Math.round(pureAmount * gradeRate);
        if (change <= 0) {
            return new EarnPointResponseDto(0, latestTotal, PointReason.ORDER);
        }

        int newTotal = latestTotal + change;

        PointHistory history = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.ORDER,
                change,
                newTotal,
                orderId,
                null,
                null,
                getDefaultExpireAt()   // 결제 적립 유효기간: 1년
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.ORDER);
    }

    // 4. 포인트 사용
    // -> 주문/결제 시 바로 차감 및 디비에 저장, 이후 결제 취소 발생하면 따로 useCancel 발생으로 복구
    // -> 만료 스케줄러와 동시 실행될 가능성 고려
    @Override
    public EarnPointResponseDto usePoint(UsePointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        Integer useAmountObj = dto.getUseAmount();

        if (userId == null) {
            throw new MissingRequiredFieldException(FIELD_USER_ID);
        }
        if (orderId == null) {
            throw new MissingRequiredFieldException(FIELD_ORDER_ID);
        }
        if (useAmountObj == null) {
            throw new MissingRequiredFieldException(FIELD_USE_AMOUNT);
        }

        int useAmount = useAmountObj;

        // 1) 검증
        // 주문에서 허용한 최대 사용 가능 포인트 범위 검증
//        pointHistoryValidator.validatePointRange(useAmount, allowedMaxUseAmount);
        // 양수 검증
        pointHistoryValidator.validatePositiveAmount(useAmount, "사용 포인트는 0보다 커야 합니다.");
        // 같은 주문에 대한 중복 사용 방지
        pointHistoryValidator.validateUseNotDuplicated(orderId);
        int latestTotal = getLatestTotalForUpdate(userId);
        // 잔액 부족 검증
        if (latestTotal < useAmount) {
            throw new InsufficientPointException(latestTotal, useAmount);
        }

        Users user = userReferenceLoader.getReference(userId);

        // 2) FIFO 로직을 위한 데이터 조회 (만료일 적립건 제외)
        int remainToUse = useAmount; // 이번 주문에서 사용하기로 한 포인트
        LocalDateTime now = LocalDateTime.now();
        List<PointHistory> earnRows = // 락 걸린 적립 row 조회
                // 잔액 확인: '적립(+)은 되었지만 아직 남아있는 포인트'만 조회 & 만료일이 오늘에 가장 가까운 것부터 리스트의 맨 위에 배치
                pointHistoryRepository.findPointsForUsage(userId, now);

        // 3) FIFO방식으로 remaining_point 차감
        for (PointHistory row : earnRows) {
            // 남은 사용량(remainToUse)이 0이 될 때까지
            if (remainToUse <= 0) {
                break; // usePoint가 0이 될 때까지 계속 반복
            }
            Integer remaining = row.getRemainingPoint();
            if (remaining == null || remaining <= 0) {
                continue;
            }
            // 각 적립 이력의 remainingPoint를 줄여 나간다.
            int usedHere = Math.min(remaining, remainToUse); // 포인트 차감
            row.setRemainingPoint(remaining - usedHere); // 잔액 갱신
            remainToUse -= usedHere; // 사용자 사용 총량 갱신
        }

        // 4) 무결성 확인
        if (remainToUse > 0) {
            // 락을 걸었는데도 부족하다면,
            // totalPoints와 remainingPoint 합 불일치 데이터가 이미 깨져있을 가능성이 큼
            throw new PointBalanceIntegrityException();
        }

        // 차감된 원장(earned rows) 변경사항을 확정 반영
        pointHistoryRepository.saveAll(earnRows);

        // 5) 사용 이력 row 생성
        int newTotal = latestTotal - useAmount;
        PointHistory useHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.USE,
                -useAmount,
                newTotal,
                orderId,
                null,
                null
        );
        pointHistoryRepository.save(useHistory);

        return new EarnPointResponseDto(-useAmount, newTotal, PointReason.USE);
    }

    // 4-1. 결제 취소 시 포인트 롤백 -> 비동기 방식으로 처리해아 함 -> rabbitMQ 적용
    @Override
    public EarnPointResponseDto useCancel(Long orderId, Long userId) {
        if (orderId == null) {
            throw new MissingRequiredFieldException(FIELD_ORDER_ID);
        }
        if (userId == null) {
            throw new MissingRequiredFieldException(FIELD_USER_ID);
        }

        // ===== 1) 취소 원복(사용포인트 복구) 멱등 =====
        boolean alreadyRolledBack =
                pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.REFUND);

        // ===== 2) 취소 시 구매적립 회수 멱등 =====
        boolean alreadyReclaimed =
                pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId,
                        PointReason.ORDER_RECLAIM);

        // 둘 다 끝났으면 현재 total 기준으로 그대로 반환
        int currentTotal = getLatestTotalForUpdate(userId);
        if (alreadyRolledBack && alreadyReclaimed) {
            return new EarnPointResponseDto(0, currentTotal, PointReason.REFUND);
        }

        Users user = userReferenceLoader.getReference(userId);

        int netChange = 0;
        int latestTotal = currentTotal;

        // ===== A) 사용포인트 복구(취소) =====
        if (!alreadyRolledBack) {
            int usedSum = pointHistoryRepository.sumUsedPointByOrder(orderId, PointReason.USE);

            // 포인트를 안 쓴 주문이면 복구 없음 (단, 아래 구매적립 회수는 별개로 수행 가능)
            if (usedSum > 0) {
                int newTotal = latestTotal + usedSum;

                PointHistory rollbackHistory = pointHistoryMapper.toEarnEntity(
                        user,
                        PointReason.REFUND,      // <-- FAILED가 아니라 REFUND로 기록
                        usedSum,
                        newTotal,
                        orderId,
                        null,
                        null,                    // refundId null == 취소 케이스
                        getCancelRefundExpireAt()
                );

                pointHistoryRepository.save(rollbackHistory);

                netChange += usedSum;
                latestTotal = newTotal;
            }
        }

        // ===== B) 구매 적립(ORDER) 회수(취소) =====
        if (!alreadyReclaimed) {
            int newTotal = reclaimOrderEarnRemainingForCancel(user, orderId, latestTotal);
            netChange += (newTotal - latestTotal); // newTotal - latestTotal 는 음수(차감)
            latestTotal = newTotal;
        }

        return new EarnPointResponseDto(netChange, latestTotal, PointReason.REFUND);
    }

    // useCancel Helper
    private int reclaimOrderEarnRemainingForCancel(Users user, Long orderId, int latestTotal) {
        // 멱등(취소): orderId + ORDER_RECLAIM + refundId null
        boolean alreadyReclaimed =
                pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId,
                        PointReason.ORDER_RECLAIM);
        if (alreadyReclaimed) {
            return latestTotal;
        }

        // 이 주문으로 지급된 ORDER 적립 row 조회
        List<PointHistory> orderEarnRows =
                pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.ORDER);

        if (orderEarnRows.isEmpty()) {
            return latestTotal;
        }

        // "남아있는 적립분(remainingPoint)"만 회수(안전한 최소 정책)
        int reclaimAmount = orderEarnRows.stream()
                .mapToInt(r -> r.getRemainingPoint() == null ? 0 : r.getRemainingPoint())
                .sum();

        if (reclaimAmount <= 0) {
            return latestTotal;
        }

        // ORDER row들의 remainingPoint를 0으로 만든다(회수 확정)
        for (PointHistory row : orderEarnRows) {
            Integer remaining = row.getRemainingPoint();
            if (remaining != null && remaining > 0) {
                row.setRemainingPoint(0);
            }
        }
        pointHistoryRepository.saveAll(orderEarnRows);

        int newTotal = latestTotal - reclaimAmount;

        // 회수(-) 이력 남김
        PointHistory reclaimHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.ORDER_RECLAIM,
                -reclaimAmount,
                newTotal,
                orderId,
                null,
                null // refundId null == 취소 케이스
        );
        pointHistoryRepository.save(reclaimHistory);

        return newTotal;
    }

    // 5. 포인트 반환 (반품)
    @Override
    public EarnPointResponseDto refundPoint(RefundPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        Long refundId = dto.getRefundId();
        int usedPoint = dto.getUsedPoint() != null ? dto.getUsedPoint() : 0;
        int returnAmount = dto.getRefundAmount() != null ? dto.getRefundAmount() : 0;

        if (userId == null) {
            throw new MissingRequiredFieldException(FIELD_USER_ID);
        }
        if (orderId == null) {
            throw new MissingRequiredFieldException(FIELD_ORDER_ID);
        }
        if (refundId == null) {
            throw new MissingRequiredFieldException(FIELD_REFUND_ID);
        }

        // 5-1. 동일 refundId 중복 처리 방지
        pointHistoryValidator.validateReturnNotDuplicated(refundId);

        Users user = userReferenceLoader.getReference(userId);
        int beforeTotal = getLatestTotalForUpdate(userId); // 현재 총 포인트
        int latestTotal = beforeTotal;
        // 둘 다 0이면 변화 없음
        if (usedPoint == 0 && returnAmount == 0) {
            return new EarnPointResponseDto(0, beforeTotal, PointReason.REFUND);
        }
        // 양수 검증
        if (usedPoint < 0) {
            throw new InvalidRefundPointException("복구 포인트는 0 이상이어야 합니다.");
        }
        if (returnAmount < 0) {
            throw new InvalidRefundPointException("환불 금액은 0 이상이어야 합니다.");
        }

        // 5-2. 이 주문에서 실제 사용된 포인트 합계를 조회
        int usedSum = pointHistoryRepository.sumUsedPointByOrder(orderId, PointReason.USE);
        // 사용 이력이 존재하지 않는데 usedPoint > 0이면 오류
        if (usedPoint > 0 && usedSum <= 0) {
            throw new InvalidRefundPointException("해당 주문에서 사용된 포인트가 없습니다.");
        }
        // 사용 요청 포인트가 실제 사용 포인트 초과하면 오류
        if (usedPoint > usedSum) {
            throw new RefundPointRangeExceededException(usedPoint, usedSum);
        }

        // 5-3. 사용 포인트 복구
        if (usedPoint > 0) {
            int newTotal = latestTotal + usedPoint;

            PointHistory restoreHistory = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.REFUND,
                    usedPoint,
                    newTotal,
                    orderId,
                    null,
                    refundId,
                    getDefaultExpireAt()
            );

            pointHistoryRepository.save(restoreHistory);
            latestTotal = newTotal;
        }

        // 5-4. 반품 금액만큼 포인트 적립 (+returnAmount)
        if (returnAmount > 0) {
            pointHistoryValidator.validatePositiveAmount(returnAmount, "반품 금액은 0보다 커야 합니다.");

            int newTotal = latestTotal + returnAmount;
            PointHistory refundAmountHistory = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.REFUND,
                    returnAmount,
                    newTotal,
                    orderId,
                    null,
                    refundId,
                    null // 현금성 포인트는 만료 없음
            );
            pointHistoryRepository.save(refundAmountHistory);
            latestTotal = newTotal;
        }

        // ===== 5-5) 반품 시 구매 적립(ORDER) 회수(remainingPoint만) =====
        latestTotal = reclaimOrderEarnRemainingForRefund(user, orderId, refundId, latestTotal);

        // 최종 변화량
        int netChange = latestTotal - beforeTotal;
        return new EarnPointResponseDto(netChange, latestTotal, PointReason.REFUND);
    }

    // refundPoint Helper
    private int reclaimOrderEarnRemainingForRefund(Users user, Long orderId, Long refundId, int latestTotal) {
        // 멱등(반품): orderId + refundId + ORDER_RECLAIM
        boolean alreadyReclaimed = pointHistoryRepository.existsByOrderIdAndRefundIdAndPointReason(
                orderId, refundId, PointReason.ORDER_RECLAIM
        );
        if (alreadyReclaimed) {
            return latestTotal;
        }

        List<PointHistory> orderEarnRows =
                pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.ORDER);

        if (orderEarnRows.isEmpty()) {
            return latestTotal;
        }

        int reclaimAmount = orderEarnRows.stream()
                .mapToInt(r -> r.getRemainingPoint() == null ? 0 : r.getRemainingPoint())
                .sum();

        if (reclaimAmount <= 0) {
            return latestTotal;
        }

        for (PointHistory row : orderEarnRows) {
            Integer remaining = row.getRemainingPoint();
            if (remaining != null && remaining > 0) {
                row.setRemainingPoint(0);
            }
        }
        pointHistoryRepository.saveAll(orderEarnRows);

        int newTotal = latestTotal - reclaimAmount;

        PointHistory reclaimHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.ORDER_RECLAIM,
                -reclaimAmount,
                newTotal,
                orderId,
                null,
                refundId
        );
        pointHistoryRepository.save(reclaimHistory);

        return newTotal;
    }

    // 6. 포인트 자동 만료 처리
    // -> 문제점: 동시성(스케줄러 vs 사용자 액션) 정도
    @Override
    public void expirePoints(Long userId) {

        int beforeTotal = getLatestTotalForUpdate(userId);

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
        pointHistoryRepository.saveAll(expiredRows);

        int newTotal = beforeTotal - expireTotal;

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

    // 7. 7일 내 소멸 예정 조회
    @Override
    public ExpiringPointResponseDto getExpiringPoints(Long userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plusDays(days);

        List<PointHistory> expiredRows = pointHistoryRepository.findSoonExpiringPoints(
                userId, now, to);

        int sum = expiredRows.stream().mapToInt(row -> row.getRemainingPoint() == null ? 0 : row.getRemainingPoint())
                .sum();

        return new ExpiringPointResponseDto(sum, now, to);
    }

    // 8. 관리자 수동 포인트 지급/차감
    @Override
    public EarnPointResponseDto adjustPointByAdmin(PointHistoryAdminAdjustRequestDto dto) {
        Long userId = dto.getUserId();
        int amount = dto.getAmount();

        pointHistoryValidator.validateAdjustAmountNotZero(amount);
        pointHistoryValidator.validatePositiveAmount(Math.abs(amount), "조정 포인트는 0보다 커야 합니다.");

        int latestTotal = getLatestTotalForUpdate(userId);
        int newTotal = latestTotal + amount;
        if (newTotal < 0) {
            throw new AdminAdjustPointNegativeBalanceException(latestTotal, amount);
        }

        Users user = userReferenceLoader.getReference(userId);

        // 적립(+) 케이스
        if (amount > 0) {
            PointHistory history = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.ADMIN_ADJUST,
                    amount,
                    newTotal,
                    null,
                    null,
                    null,
                    getDefaultExpireAt()
            );
            pointHistoryRepository.save(history);
            return new EarnPointResponseDto(amount, newTotal, PointReason.ADMIN_ADJUST);
        }

        // 차감(-) 케이스
        int deductAmount = Math.abs(amount);

        if (latestTotal < deductAmount) {
            throw new AdminAdjustPointNegativeBalanceException(latestTotal, amount);
        }

        // FIFO: 기존 적립된 포인트들의 remainingPoint 깎기
        List<PointHistory> earnRows = pointHistoryRepository.findPointsForUsage(userId, LocalDateTime.now());

        int remainToDeduct = deductAmount;

        for (PointHistory row : earnRows) {
            if (remainToDeduct <= 0) {
                break;
            }
            int remaining = row.getRemainingPoint() == null ? 0 : row.getRemainingPoint();
            if (remaining > 0) {
                int usedHere = Math.min(remaining, remainToDeduct);
                row.setRemainingPoint(remaining - usedHere);
                remainToDeduct -= usedHere;
            }
        }

        if (remainToDeduct > 0) {
            throw new PointBalanceIntegrityException();
        }

        // 변경된 remainingPoint 저장
        pointHistoryRepository.saveAll(earnRows);

        // 차감 이력 생성
        PointHistory history = pointHistoryMapper.toUseOrDeductEntity(
                user, PointReason.ADMIN_ADJUST, amount, newTotal, null, null, null
        );

        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(amount, newTotal, PointReason.ADMIN_ADJUST);
    }

    // 9. 회원탈퇴 시 포인트 삭제 -> 전액 소멸(WITHDRAW) 이력으로 남긴 뒤 잔액만 0 만들기
    @Override
    public void expireAllPointsForWithdraw(Long userId) {
        int beforeTotal = getLatestTotalForUpdate(userId);

        List<PointHistory> rows = pointHistoryValidator.getAllRemainingEarnRows(userId);
        if (rows.isEmpty()) {
            return;
        }

        int expireTotal = rows.stream()
                .mapToInt(row -> row.getRemainingPoint() == null ? 0 : row.getRemainingPoint())
                .sum();

        if (expireTotal <= 0) {
            return;
        }

        for (PointHistory row : rows) {
            row.setRemainingPoint(0);
        }
        pointHistoryRepository.saveAll(rows);

        int newTotal = beforeTotal - expireTotal;

        Users user = userReferenceLoader.getReference(userId);

        PointHistory expireHistory = pointHistoryMapper.toUseOrDeductEntity(
                user,
                PointReason.WITHDRAW,
                -expireTotal,
                newTotal,
                null,
                null,
                null
        );
        pointHistoryRepository.save(expireHistory);
    }

    @Override
    public PointSummaryResponseDto getMyPointSummary(Long userId) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        int currentTotal = getLatestTotalForRead(userId);
        int earnedThisMonth = pointHistoryRepository.sumEarnedInPeriod(userId, from, now);
        int usedThisMonth = pointHistoryRepository.sumUsedInPeriod(userId, from, now);

        ExpiringPointResponseDto expiring = getExpiringPoints(userId, 7);

        return new PointSummaryResponseDto(
                currentTotal,
                earnedThisMonth,
                usedThisMonth,
                expiring.getExpiringAmount(),
                from,
                now
        );
    }

}