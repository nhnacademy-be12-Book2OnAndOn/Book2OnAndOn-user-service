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
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.support.pointHistory.PointCalculationHelper;
import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryMapper;
import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryValidator;
import com.example.book2onandonuserservice.point.support.pointHistory.UserReferenceLoader;
import com.example.book2onandonuserservice.user.domain.entity.Users;
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
    private final UserReferenceLoader userReferenceLoader;
    private final PointCalculationHelper pointCalculationHelper;
    private final PointHistoryMapper pointHistoryMapper;
    private final PointHistoryValidator pointHistoryValidator;

    // TODO 탈퇴 시 포인트 삭제(userId만 받기)
    // TODO view 필요한 정보 -> 보유 포인트(totalPoint), 이번달 적립, 이번달 사용, 소멸 예정 포인트, 포인트 내역(필터: 전체, 사용, 적립)

    // ===== 공통 유틸 =====
    // 1. 현재 보유 포인트 "숫자만" 필요할 때 사용하는 내부 헬퍼
    private int getLatestTotal(Long userId) {
        return pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId)
                .map(PointHistory::getTotalPoints)
                .orElse(0);
    }

    // 2. 포인트 적립 시 만료일 (기본 1년)
    // 2025-01-01 00:00:00 지급 -> 2026-01-01 23:59:59.999999까지 사용 가능
    private LocalDateTime getDefaultExpireAt() {
        LocalDate expireDate = LocalDate.now().plusYears(1);
//        return expireDate.atTime(23, 59, 59, 999999000);
        return expireDate.atStartOfDay().minusNanos(1);
    }

    // 3. 회원가입 적립 전용 만료일 (10일)
    private LocalDateTime getSignupExpireAt() {
        LocalDate expireDate = LocalDate.now().plusDays(10);
//        return expireDate.atTime(23, 59, 59, 999999000);
        return expireDate.atStartOfDay().minusNanos(1);
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

    // 3. 포인트 적립 (+)
    // 3-1. 회원가입 적립
    @Override
    public EarnPointResponseDto earnSignupPoint(Long userId) {
        // 유저 로딩
        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

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
                null, // returnId
                getSignupExpireAt() // 전용 만료일 처리
        );
        pointHistoryRepository.save(history);

        return new EarnPointResponseDto(change, newTotal, PointReason.SIGNUP);
    }

    // 3-2. 리뷰 작성 적립 (일반 / 사진)
//    @Override
//    public EarnPointResponseDto earnReviewPoint(EarnReviewPointRequestDto dto) {
//
//        Long userId = dto.getUserId();
//        Long reviewId = dto.getReviewId();
//        Long orderId = dto.getOrderId();
//        PointReviewType reviewType = dto.getReviewType();
//
//        // 1) 리뷰 중복 적립 방지
//        pointHistoryValidator.validateReviewNotDuplicated(reviewId);
//
//        Users user = userReferenceLoader.getReference(userId);
//        int latestTotal = getLatestTotal(userId);
//
//        // 2) 리뷰 타입에 따른 정책명 결정
//        String policyName = switch (reviewType) {
//            case PHOTO -> "REVIEW_PHOTO";
//            case TEXT -> "REVIEW_TEXT";
//        };
//
//        int change = pointCalculationHelper.calculateByPolicyName(policyName);
//        if (change <= 0) {
//            return new EarnPointResponseDto(0, latestTotal, PointReason.REVIEW);
//        }
//
//        int newTotal = latestTotal + change;
//
//        PointHistory history = pointHistoryMapper.toEarnEntity(
//                user,
//                PointReason.REVIEW,
//                change,
//                newTotal,
//                orderId,
//                reviewId,
//                null,
//                getDefaultExpireAt()
//        );
//        pointHistoryRepository.save(history);
//
//        return new EarnPointResponseDto(change, newTotal, PointReason.REVIEW);
//    }
    @Override
    public EarnPointResponseDto earnReviewPoint(EarnReviewPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long reviewId = dto.getReviewId();
        boolean hasImage = dto.isHasImage();

        // 1) 리뷰 중복 적립 방지
        pointHistoryValidator.validateReviewNotDuplicated(reviewId);

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

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
//        return ResponseEntity.ok().build(); // TODO 리턴타입을 EarnPointResponseDto? Void?
    }

    // 3-3. 도서 결제 적립 (유저 등급별 기본 적립률 책정)
    @Override
    public EarnPointResponseDto earnOrderPoint(EarnOrderPointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        int pureAmount = dto.getPureAmount();
        Double gradeRate = dto.getPointAddRate();

        // 1) 주문 중복 적립 방지
        pointHistoryValidator.validateOrderEarnNotDuplicated(orderId);

        // 2) 금액/비율 검증
        pointHistoryValidator.validatePositiveAmount(pureAmount, "주문 금액은 0보다 커야 합니다.");
        if (gradeRate == null || gradeRate <= 0) {
            throw new IllegalArgumentException("등급 적립률은 0보다 커야 합니다.");
        }

        Users user = userReferenceLoader.getReference(userId);
        int latestTotal = getLatestTotal(userId);

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
    // “결제 성공이 확정된 시점(구매확정 후)”에만 적용되도록 -> 이는 주문에서 요청을 줘야 알 수 있음. 그니까 그냥 포인트는 orderId만 받아 사용하게끔만...
    // TODO 만료 스케줄러와 동시 실행될 가능성 고려
    @Override
    public EarnPointResponseDto usePoint(UsePointRequestDto dto) {

        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        int useAmount = dto.getUseAmount();
        int allowedMaxUseAmount = dto.getAllowedMaxUseAmount(); // 주문에서 받아야 할 듯

        // 1) 검증
        // 주문에서 허용한 최대 사용 가능 포인트 범위 검증
        pointHistoryValidator.validatePointRange(useAmount, allowedMaxUseAmount);
        // 양수 검증
        pointHistoryValidator.validatePositiveAmount(useAmount, "사용 포인트는 0보다 커야 합니다.");
        // 같은 주문에 대한 중복 사용 방지
        pointHistoryValidator.validateUseNotDuplicated(orderId);

        int latestTotal = getLatestTotal(userId);
        // latestTotal >= useAmount인지 확인
        if (latestTotal < useAmount) {
            throw new InsufficientPointException(latestTotal, useAmount);
        }

        Users user = userReferenceLoader.getReference(userId);

        // 2) FIFO 방식으로 remaining_point 차감
        int usePoint = useAmount; // 이번 주문에서 사용하기로 한 포인트
        List<PointHistory> earnRows =
                // 잔액 확인: '적립(+)은 되었지만 아직 남아있는 포인트'만 조회
                // & 만료일이 오늘에 가장 가까운 것부터 리스트의 맨 위에 배치
                pointHistoryRepository.findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
                        userId,
                        0,
                        0
                );

        // 3) 위의 리스트 earnRows을 순회
        for (PointHistory row : earnRows) {
            // 남은 사용량(remainToUse)이 0이 될 때까지
            if (usePoint <= 0) {
                break; // usePoint가 0이 될 때까지 계속 반복
            }
            Integer remaining = row.getRemainingPoint();
            if (remaining == null || remaining <= 0) {
                continue;
            }
            // 각 적립 이력의 remainingPoint를 줄여 나간다.
            int usedHere = Math.min(remaining, usePoint); // 포인트 차감
            row.setRemainingPoint(remaining - usedHere); // 잔액 갱신
            usePoint -= usedHere; // 사용자 사용 총량 갱신
        }

        if (usePoint > 0) {
            throw new IllegalStateException("remaining_point 합계가 부족합니다. 데이터 정합성을 확인하세요.");
        }

        // 4) 사용 이력 row 생성
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

    // 4-1. 결제 취소 시 포인트 롤백
    @Override
    public EarnPointResponseDto useCancle(Long orderId, Long userId) {
        // 1) 이 주문에 대한 USE 이력 조회 (한 건만 가져와도 userId는 동일)
        List<PointHistory> useHistories =
                pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.USE);

        int beforeTotal = getLatestTotal(userId);

        if (useHistories.isEmpty()) {
            // 이미 취소되었거나, 애초에 포인트를 사용하지 않은 주문
            // 필요에 따라 0P 변화로 리턴하거나 예외 처리 선택
            return new EarnPointResponseDto(0, beforeTotal, PointReason.REFUND);
        }

        Users user = useHistories.get(0).getUser();
        Long historyUserId = user.getUserId();
        if (!historyUserId.equals(userId)) {
            throw new IllegalArgumentException(
                    "해당 주문의 포인트 사용 이력과 요청 userId가 일치하지 않습니다. " +
                            "orderId=" + orderId + ", historyUserId=" + historyUserId + ", requestUserId=" + userId
            );
        }

        // 2) 이 주문에서 실제로 사용된 포인트 합계 구하기
        int usedSum = useHistories.stream()
                .mapToInt(h -> -h.getPointHistoryChange()) // 음수를 양수로
                .sum();

        if (usedSum <= 0) {
            // USE 이력이 이상한 상황 방어
            return new EarnPointResponseDto(0, beforeTotal, PointReason.REFUND);
        }

        // 3) 포인트 복구 이력 추가 (만료일은 정책에 따라: 기본 1년 or 원래 만료 연동 등)
        int newTotal = beforeTotal + usedSum;

        PointHistory rollbackHistory = pointHistoryMapper.toEarnEntity(
                user,
                PointReason.REFUND,      // 또는 PointReason.CANCEL 등 별도 사유 추가 가능
                usedSum,
                newTotal,
                orderId,
                null,
                null,
                getDefaultExpireAt()     // 정책 선택 지점
        );
        pointHistoryRepository.save(rollbackHistory);

        return new EarnPointResponseDto(usedSum, newTotal, PointReason.REFUND);
    }

    // 5. 포인트 반환 (반품)
    // 리뷰 삭제는 구현 안한다고 하셔서 포인트 반환도 리뷰 반환은 제외
    @Override
    // TODO 이미 사용해버려서 remaining이 0이면 회수할 시 전체 포인트가 마이너스가 됨. -> 구매 확정 처리로 방지
    public EarnPointResponseDto refundPoint(RefundPointRequestDto dto) {

        //TODO 현재 코드에서는 usedPoint가 “이 주문에서 실제로 사용한 포인트”인지 검증하지 않는다.
        // PointHistory에서 PointReason.USE & orderId로 실제 사용 포인트를 직접 조회해서 쓰거나,
        // 최소한 usedPoint <= 해당 주문 USE 이력의 절대값 정도는 검증하는 게 좋을 듯.
        Long userId = dto.getUserId();
        Long orderId = dto.getOrderId();
        Long returnId = dto.getReturnId();
        int usedPoint = dto.getUsedPoint() != null ? dto.getUsedPoint() : 0;
        int returnAmount = dto.getReturnAmount() != null ? dto.getReturnAmount() : 0;

        // 동일 returnId 중복 처리 방지
        pointHistoryValidator.validateReturnNotDuplicated(returnId);

        Users user = userReferenceLoader.getReference(userId);
        int beforeTotal = getLatestTotal(userId);
        int latestTotal = beforeTotal;

        // 5-1. 사용 포인트 복구 (+usedPoint)
        // TODO usedPoint > 0일 때, 해당 주문의 USE 이력 합(useSum)을 구해서 usedPoint <= useSum인지 검증.
        //  아니면 validatePointRange와 같은 검증 헬퍼 하나 더 두는 방식.
        if (usedPoint > 0) {
            pointHistoryValidator.validatePositiveAmount(usedPoint, "복구 포인트는 0보다 커야 합니다.");

            int usedSum = pointHistoryRepository.sumUsedPointByOrder(orderId, PointReason.USE);
            if (usedSum <= 0) {
                throw new IllegalStateException("해당 주문에서 사용된 포인트가 없습니다.");
            }
            if (usedPoint > usedSum) {
                throw new IllegalArgumentException("복구 요청 포인트가 실제 사용 포인트를 초과합니다.");
            }

            int newTotal = latestTotal + usedPoint;
            PointHistory restoreHistory = pointHistoryMapper.toEarnEntity(
                    user,
                    PointReason.REFUND,
                    usedPoint,
                    newTotal,
                    orderId,
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
                    orderId,
                    null,
                    returnId,
                    getDefaultExpireAt()
            );
            pointHistoryRepository.save(refundAmountHistory);
            latestTotal = newTotal;
        }

        // 5-3. 이 주문에 대해 적립되었던 리뷰 포인트 회수
//        List<PointHistory> orderHistories = pointHistoryRepository.findByOrderId(orderId);
//        int reviewEarnSum = 0;
//        for (PointHistory h : orderHistories) {
//            if (h.getPointReason() == PointReason.REVIEW && h.getPointHistoryChange() > 0) {
//                Integer remaining = h.getRemainingPoint();
//                if (remaining != null && remaining > 0) {
//                    reviewEarnSum += remaining;
//                    h.setRemainingPoint(0);   // 이미 회수했으므로 더 이상 사용/만료 대상이 아님
//                }
//            }
//        }
//
//        if (reviewEarnSum > 0) {
//            int newTotal = latestTotal - reviewEarnSum;
//            PointHistory cancelReviewHistory = pointHistoryMapper.toUseOrDeductEntity(
//                    user,
//                    PointReason.REFUND,   // 회수도 REFUND 범주로 기록
//                    -reviewEarnSum,
//                    newTotal,
//                    orderId,
//                    null,
//                    returnId
//            );
//            pointHistoryRepository.save(cancelReviewHistory);
//            latestTotal = newTotal;
//        }

        int netChange = latestTotal - beforeTotal;
        return new EarnPointResponseDto(netChange, latestTotal, PointReason.REFUND);
    }

    // 6. 포인트 자동 만료 처리
    // TODO 현실적인 리스크는 동시성(스케줄러 vs 사용자 액션) 정도
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

    // 7. 7일 내 소멸 예정 조회
    // TODO 스케줄러/만료 처리와 마찬가지로 “일 단위”로 끊고 싶다면:
    //  from = LocalDate.now().atStartOfDay()
    //  to = from.plusDays(days).atStartOfDay()
    @Override
    public ExpiringPointResponseDto getExpiringPoints(Long userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plusDays(days);

        List<PointHistory> expiredRows = pointHistoryRepository.findByUserUserIdAndPointExpiredDateBetweenAndRemainingPointGreaterThan(
                userId, now, to, 0);

        int sum = expiredRows.stream().mapToInt(row -> row.getRemainingPoint() == null ? 0 : row.getRemainingPoint())
                .sum();

        return new ExpiringPointResponseDto(sum, now, to);
    }

    // 8. 관리자 수동 포인트 지급/차감
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

    // 9. 회원탈퇴 시 포인트 삭제 -> 전액 소멸(WITHDRAW) 이력으로 남긴 뒤 잔액만 0 만들기
    @Override
    public void expireAllPointsForWithdraw(Long userId) {
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

        int latestTotal = getLatestTotal(userId);
        int newTotal = latestTotal - expireTotal;

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

        int currentTotal = getLatestTotal(userId);
        int earnedThisMonth = pointHistoryRepository.sumEarnedInPeriod(userId, from, now);
        int usedThisMonth = pointHistoryRepository.sumUsedInPeriod(userId, from, now);

        // 이미 구현돼 있는 메서드 재사용
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