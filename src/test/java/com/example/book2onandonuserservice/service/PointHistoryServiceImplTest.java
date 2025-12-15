package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.AdminAdjustPointNegativeBalanceException;
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.InvalidAdminAdjustPointException;
import com.example.book2onandonuserservice.point.exception.InvalidRefundPointException;
import com.example.book2onandonuserservice.point.exception.PointBalanceIntegrityException;
import com.example.book2onandonuserservice.point.exception.RefundPointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.point.exception.UserIdMismatchException;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.service.PointHistoryServiceImpl;
import com.example.book2onandonuserservice.point.support.pointhistory.PointCalculationHelper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryMapper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryValidator;
import com.example.book2onandonuserservice.point.support.pointhistory.UserReferenceLoader;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceImplTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointHistoryMapper pointHistoryMapper;
    @Mock
    private PointHistoryValidator pointHistoryValidator;
    @Mock
    private UserReferenceLoader userReferenceLoader;
    @Mock
    private PointCalculationHelper pointCalculationHelper;

    @InjectMocks
    private PointHistoryServiceImpl pointHistoryService;

    private Users user;

    @BeforeEach
    void setup() {
        user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);
    }

    // 조회 테스트
    @Test
    void getMyPointHistory_success() {
        Pageable pageable = PageRequest.of(0, 10);

        PointHistory h1 = mock(PointHistory.class);
        PointHistory h2 = mock(PointHistory.class);

        PointHistoryResponseDto dto1 = mock(PointHistoryResponseDto.class);
        PointHistoryResponseDto dto2 = mock(PointHistoryResponseDto.class);

        Page<PointHistory> page = new PageImpl<>(List.of(h1, h2));

        when(pointHistoryRepository
                .findAllByUserUserIdOrderByPointCreatedDateDesc(1L, pageable))
                .thenReturn(page);

        when(pointHistoryMapper.toDto(h1)).thenReturn(dto1);
        when(pointHistoryMapper.toDto(h2)).thenReturn(dto2);

        Page<PointHistoryResponseDto> result =
                pointHistoryService.getMyPointHistory(1L, pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void getMyPointHistoryByType_EARN() {
        Page<PointHistory> page = new PageImpl<>(List.of(new PointHistory()));
        when(pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeGreaterThanOrderByPointCreatedDateDesc(
                        eq(1L), eq(0), any()))
                .thenReturn(page);

        when(pointHistoryMapper.toDto(any())).thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res = pointHistoryService.getMyPointHistoryByType(1L, "EARN",
                PageRequest.of(0, 10));
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getMyPointHistoryByType_USE() {
        Page<PointHistory> page = new PageImpl<>(List.of(new PointHistory()));
        when(pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeLessThanOrderByPointCreatedDateDesc(
                        eq(1L), eq(0), any()))
                .thenReturn(page);
        when(pointHistoryMapper.toDto(any())).thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res =
                pointHistoryService.getMyPointHistoryByType(1L, "USE", PageRequest.of(0, 10));

        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getMyPointHistoryByType_ELSE() {
        Page<PointHistory> page = new PageImpl<>(List.of(new PointHistory()));
        when(pointHistoryRepository
                .findAllByUserUserIdOrderByPointCreatedDateDesc(eq(1L), any()))
                .thenReturn(page);

        when(pointHistoryMapper.toDto(any())).thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res =
                pointHistoryService.getMyPointHistoryByType(1L, "ETC", PageRequest.of(0, 10));

        assertEquals(1, res.getTotalElements());
    }

    @Test
    void getMyCurrentPoint_success() {
        PointHistory history = new PointHistory();
        history.setTotalPoints(150);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(history));

        CurrentPointResponseDto dto = pointHistoryService.getMyCurrentPoint(1L);

        assertEquals(150, dto.getCurrentPoint());
    }

    // SIGNUP 적립
    @Test
    void earnSignupPoint_success() {
        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
                .thenReturn(false);

        when(pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.empty());

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP))
                .thenReturn(50);

        PointHistory savedEntity = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.SIGNUP),
                eq(50),
                eq(50),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(savedEntity);

        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
    }

    @Test
    void earnSignupPoint_noChange() {
        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
                .thenReturn(false);

        when(pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(new PointHistory()));

        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP))
                .thenReturn(0);

        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);

        assertEquals(0, dto.getChangedPoint());
    }

    @Test
    void earnSignupPoint_duplicate_throws() {
        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
                .thenReturn(true);

        assertThrows(SignupPointAlreadyGrantedException.class,
                () -> pointHistoryService.earnSignupPoint(1L));
    }

    // REVIEW 적립
    @Test
    void earnReviewPoint_text_success() {
        EarnReviewPointRequestDto req =
                new EarnReviewPointRequestDto(1L, 101L, false);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.empty());

        when(pointCalculationHelper.calculateByPolicyName("REVIEW_TEXT"))
                .thenReturn(50);

        PointHistory entity = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REVIEW),
                eq(50),
                eq(50),
                isNull(),        // orderId
                eq(101L),        // reviewId
                isNull(),        // returnId
                any()
        )).thenReturn(entity);

        EarnPointResponseDto dto = pointHistoryService.earnReviewPoint(req);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
        verify(pointHistoryRepository).save(entity);
    }

    @Test
    void earnReviewPoint_photo_success() {
        EarnReviewPointRequestDto req =
                new EarnReviewPointRequestDto(1L, 101L, true);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.empty());

        when(pointCalculationHelper.calculateByPolicyName("REVIEW_PHOTO"))
                .thenReturn(50);

        PointHistory entity = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REVIEW),
                eq(50),
                eq(50),
                isNull(),        // orderId
                eq(101L),        // reviewId
                isNull(),        // returnId
                any()
        )).thenReturn(entity);

        EarnPointResponseDto dto = pointHistoryService.earnReviewPoint(req);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
        verify(pointHistoryRepository).save(entity);
    }

    @Test
    void earnReviewPoint_changeZero() {
        EarnReviewPointRequestDto dto = new EarnReviewPointRequestDto(1L, 10L, false);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(10L);
        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointCalculationHelper.calculateByPolicyName("REVIEW_TEXT")).thenReturn(0);

        EarnPointResponseDto res = pointHistoryService.earnReviewPoint(dto);
        assertEquals(0, res.getChangedPoint());
    }

    @Test
    void earnReviewPoint_duplicate_throws() {
        EarnReviewPointRequestDto req =
                new EarnReviewPointRequestDto(1L, 101L, true);

        doThrow(new ReviewAlreadyRewardedException(101L))
                .when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        assertThrows(ReviewAlreadyRewardedException.class,
                () -> pointHistoryService.earnReviewPoint(req));
    }

    // ORDER 적립
    @Test
    void earnOrderPoint_success() {
        EarnOrderPointRequestDto req =
                new EarnOrderPointRequestDto(1L, 201L, 1000, 0.1);

        doNothing().when(pointHistoryValidator).validateOrderEarnNotDuplicated(201L);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.empty());
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory entity = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.ORDER),
                eq(100),
                eq(100),
                eq(201L),
                isNull(),
                isNull(),
                any()
        )).thenReturn(entity);

        EarnPointResponseDto dto = pointHistoryService.earnOrderPoint(req);

        assertEquals(100, dto.getChangedPoint());
        assertEquals(100, dto.getTotalPointAfter());
        verify(pointHistoryRepository).save(entity);
    }

    @Test
    void earnOrderPoint_changeZero() {
        EarnOrderPointRequestDto dto =
                new EarnOrderPointRequestDto(1L, 100L, 1, 0.001);

        doNothing().when(pointHistoryValidator).validateOrderEarnNotDuplicated(100L);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(anyInt(), any());

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        EarnPointResponseDto res = pointHistoryService.earnOrderPoint(dto);
        assertEquals(0, res.getChangedPoint());
    }

    @Test
    void usePoint_success_fifo() {

        UsePointRequestDto req = new UsePointRequestDto(1L, 201L, 60, 100);

        doNothing().when(pointHistoryValidator).validatePointRange(60, 100);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(60, "사용 포인트는 0보다 커야 합니다.");
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        PointHistory latest = new PointHistory();
        latest.setTotalPoints(100);

        when(pointHistoryRepository
                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        r1.setPointHistoryChange(30);

        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(50);
        r2.setPointHistoryChange(50);

        when(pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
                        1L, 0, 0
                )).thenReturn(List.of(r1, r2));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory savedUse = new PointHistory();

        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.USE),
                eq(-60),
                anyInt(),
                eq(201L),
                isNull(),
                isNull()
        )).thenReturn(savedUse);

        EarnPointResponseDto dto = pointHistoryService.usePoint(req);

        assertEquals(-60, dto.getChangedPoint());
        assertEquals(0, r1.getRemainingPoint());
        assertEquals(20, r2.getRemainingPoint());
        verify(pointHistoryRepository).save(savedUse);
    }

    @Test
    void usePoint_notEnough_throws() {
        UsePointRequestDto req = new UsePointRequestDto(1L, 201L, 100, 200);

        doNothing().when(pointHistoryValidator).validatePointRange(100, 200);
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(new PointHistory()));

        assertThrows(InsufficientPointException.class,
                () -> pointHistoryService.usePoint(req));
    }

    @Test
    void usePoint_continue_and_break() {
        UsePointRequestDto dto = new UsePointRequestDto(1L, 200L, 30, 100);

        doNothing().when(pointHistoryValidator).validatePointRange(anyInt(), anyInt());
        doNothing().when(pointHistoryValidator).validatePositiveAmount(anyInt(), any());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(any());

        PointHistory latest = new PointHistory();
        latest.setTotalPoints(50);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        PointHistory skip = new PointHistory();
        skip.setRemainingPoint(0);

        PointHistory use = new PointHistory();
        use.setRemainingPoint(50);

        when(pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
                        any(), anyInt(), anyInt()))
                .thenReturn(List.of(skip, use));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryMapper.toUseOrDeductEntity(any(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PointHistory());

        EarnPointResponseDto res = pointHistoryService.usePoint(dto);
        assertEquals(-30, res.getChangedPoint());
    }

    @Test
    void usePoint_integrityException() {
        UsePointRequestDto dto = new UsePointRequestDto(1L, 200L, 30, 100);

        doNothing().when(pointHistoryValidator).validatePointRange(anyInt(), anyInt());
        doNothing().when(pointHistoryValidator).validatePositiveAmount(anyInt(), any());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(any());

        PointHistory latest = new PointHistory();
        latest.setTotalPoints(30);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        when(pointHistoryRepository
                .findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
                        any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        assertThrows(PointBalanceIntegrityException.class, () -> pointHistoryService.usePoint(dto));
    }

    // 결제 취소
    @Test
    void useCancle_empty() {
        when(pointHistoryRepository.findByOrderIdAndPointReason(any(), any()))
                .thenReturn(List.of());

        EarnPointResponseDto res = pointHistoryService.useCancle(1L, 1L);
        assertEquals(0, res.getChangedPoint());
    }

    @Test
    void useCancle_usedSumZero() {
        PointHistory h = new PointHistory();
        h.setPointHistoryChange(0);
        h.setUser(user);

        when(pointHistoryRepository.findByOrderIdAndPointReason(any(), any()))
                .thenReturn(List.of(h));

        EarnPointResponseDto res = pointHistoryService.useCancle(1L, 1L);
        assertEquals(0, res.getChangedPoint());
    }

    @Test
    void useCancle_success_savesRollback_andReturnsRefundDto() {
        Long orderId = 10L;
        Long userId = 1L;

        // useHistories 준비: USE 이력은 보통 change가 음수
        PointHistory h1 = new PointHistory();
        h1.setPointHistoryChange(-30);
        h1.setUser(user);

        PointHistory h2 = new PointHistory();
        h2.setPointHistoryChange(-20);
        h2.setUser(user);

        when(pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.USE))
                .thenReturn(List.of(h1, h2));

        // beforeTotal (getLatestTotal)
        PointHistory latest = new PointHistory();
        latest.setTotalPoints(100);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(userId))
                .thenReturn(Optional.of(latest));

        // mapper -> rollback 엔티티
        PointHistory rollbackEntity = new PointHistory();
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REFUND),
                eq(50),          // usedSum = 30 + 20
                eq(150),         // beforeTotal 100 + 50
                eq(orderId),
                isNull(),
                isNull(),
                any()
        )).thenReturn(rollbackEntity);

        EarnPointResponseDto dto = pointHistoryService.useCancle(orderId, userId);

        verify(pointHistoryRepository).save(rollbackEntity);
        assertEquals(50, dto.getChangedPoint());
        assertEquals(150, dto.getTotalPointAfter());
        assertEquals(PointReason.REFUND, dto.getEarnReason());
    }

    @Test
    void useCancle_userIdMismatch_throws() {
        Long orderId = 10L;
        Long requestUserId = 1L;

        Users otherUser = new Users();
        ReflectionTestUtils.setField(otherUser, "userId", 999L);

        PointHistory h = new PointHistory();
        h.setPointHistoryChange(-50);
        h.setUser(otherUser);

        when(pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.USE))
                .thenReturn(List.of(h));

        assertThrows(UserIdMismatchException.class,
                () -> pointHistoryService.useCancle(orderId, requestUserId));
    }

    // 환불
    @Test
    void refundPoint_success() {
        RefundPointRequestDto req =
                new RefundPointRequestDto(1L, 201L, 301L, 20, 30);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(301L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = new PointHistory();
        latest.setTotalPoints(0);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        when(pointHistoryRepository.sumUsedPointByOrder(201L, PointReason.USE))
                .thenReturn(20);

        PointHistory saved1 = new PointHistory();
        PointHistory saved2 = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(
                any(Users.class),
                eq(PointReason.REFUND),
                eq(20),
                anyInt(),
                eq(201L),
                isNull(),
                eq(301L),
                any()
        )).thenReturn(saved1);

        when(pointHistoryMapper.toEarnEntity(any(
                        Users.class),
                eq(PointReason.REFUND),
                eq(30),
                anyInt(),
                eq(201L),
                isNull(),
                eq(301L),
                any()
        )).thenReturn(saved2);

        EarnPointResponseDto dto = pointHistoryService.refundPoint(req);

        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class));
        assertEquals(50, dto.getChangedPoint());
    }

    @Test
    void refundPoint_usedPointExceed() {
        RefundPointRequestDto dto =
                new RefundPointRequestDto(1L, 1L, 1L, 50, 0);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(any());
        when(userReferenceLoader.getReference(any())).thenReturn(user);
        when(pointHistoryRepository.sumUsedPointByOrder(any(), any()))
                .thenReturn(10);

        assertThrows(RefundPointRangeExceededException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    @Test
    void refundPoint_noUsedPoint_throws() {
        RefundPointRequestDto dto =
                new RefundPointRequestDto(1L, 10L, 20L, 0, 10);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.sumUsedPointByOrder(10L, PointReason.USE))
                .thenReturn(0);

        assertThrows(InvalidRefundPointException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    @Test
    void refundPoint_negativeReturnAmount_throws() {
        RefundPointRequestDto dto =
                new RefundPointRequestDto(1L, 10L, 20L, 10, -5);

        assertThrows(InvalidRefundPointException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    // 만료
    @Test
    void expirePoints_success() {

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        r1.setPointHistoryChange(30);

        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(20);
        r2.setPointHistoryChange(20);

        when(pointHistoryValidator.getExpiredEarnRows(1L))
                .thenReturn(List.of(r1, r2));

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(new PointHistory()));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory savedExpire = new PointHistory();

        when(pointHistoryMapper.toUseOrDeductEntity(eq(user), eq(PointReason.EXPIRE),
                eq(-50),
                anyInt(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(savedExpire);

        pointHistoryService.expirePoints(1L);

        verify(pointHistoryRepository).save(savedExpire);
        assertEquals(0, r1.getRemainingPoint());
        assertEquals(0, r2.getRemainingPoint());
    }

    @Test
    void expireAllPointsForWithdraw_success_savesWithdrawHistory() {
        // rows: remainingPoint 합이 0보다 크게 구성
        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(null); // null도 섞어서 mapToInt 분기까지 같이 커버
        PointHistory r3 = new PointHistory();
        r3.setRemainingPoint(20);

        when(pointHistoryValidator.getAllRemainingEarnRows(1L))
                .thenReturn(List.of(r1, r2, r3));

        // latestTotal (getLatestTotal 내부에서 사용)
        PointHistory latest = new PointHistory();
        latest.setTotalPoints(100);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory mapped = new PointHistory();
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.WITHDRAW),
                eq(-50),          // 30 + 0 + 20 = 50
                eq(50),           // 100 - 50 = 50
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mapped);

        pointHistoryService.expireAllPointsForWithdraw(1L);

        // remainingPoint 0으로 만드는 루프도 검증 가능
        assertEquals(0, r1.getRemainingPoint());
        assertEquals(0, r3.getRemainingPoint());

        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void expirePoints_empty() {
        when(pointHistoryValidator.getExpiredEarnRows(any()))
                .thenReturn(List.of());

        pointHistoryService.expirePoints(1L);

        verify(pointHistoryRepository, times(0)).save(any());
    }

    @Test
    void expireAllPointsForWithdraw_empty() {
        when(pointHistoryValidator.getAllRemainingEarnRows(any()))
                .thenReturn(List.of());

        pointHistoryService.expireAllPointsForWithdraw(1L);

        verify(pointHistoryRepository, times(0)).save(any());
    }

    @Test
    void getExpiringPoints_empty() {
        when(pointHistoryRepository
                .findByUserUserIdAndPointExpiredDateBetweenAndRemainingPointGreaterThan(
                        any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        ExpiringPointResponseDto res = pointHistoryService.getExpiringPoints(1L, 7);
        assertEquals(0, res.getExpiringAmount());
    }

    // 관리자 조정
    @Test
    void adjustPointByAdmin_success_add() {
        PointHistoryAdminAdjustRequestDto dto =
                new PointHistoryAdminAdjustRequestDto(1L, 30, "메모");

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(new PointHistory()));
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory saved = new PointHistory();

        when(pointHistoryMapper.toEarnEntity(eq(user), eq(PointReason.ADMIN_ADJUST),
                eq(30),
                anyInt(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(saved);

        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);

        verify(pointHistoryRepository).save(saved);
        assertEquals(30, res.getChangedPoint());
    }

    @Test
    void adjustPointByAdmin_success_deduct() {
        PointHistoryAdminAdjustRequestDto dto =
                new PointHistoryAdminAdjustRequestDto(1L, -20, "메모");

        PointHistory prev = new PointHistory();
        prev.setTotalPoints(50);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(prev));
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory saved = new PointHistory();

        when(pointHistoryMapper.toUseOrDeductEntity(eq(user), eq(PointReason.ADMIN_ADJUST),
                eq(-20),
                anyInt(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(saved);

        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);

        verify(pointHistoryRepository).save(saved);
        assertEquals(-20, res.getChangedPoint());
    }

    @Test
    void adjustPointByAdmin_amountZero_throws() {
        PointHistoryAdminAdjustRequestDto dto =
                new PointHistoryAdminAdjustRequestDto(1L, 0, "메모");

        assertThrows(InvalidAdminAdjustPointException.class,
                () -> pointHistoryService.adjustPointByAdmin(dto));
    }

    @Test
    void adjustPointByAdmin_negativeBalance_throws() {
        PointHistoryAdminAdjustRequestDto dto =
                new PointHistoryAdminAdjustRequestDto(1L, -20, "차감");

        PointHistory latest = new PointHistory();
        latest.setTotalPoints(10);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        assertThrows(AdminAdjustPointNegativeBalanceException.class,
                () -> pointHistoryService.adjustPointByAdmin(dto));
    }

    @Test
    void getMyPointSummary_cover() {
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(any()))
                .thenReturn(Optional.of(new PointHistory()));
        when(pointHistoryRepository.sumEarnedInPeriod(any(), any(), any())).thenReturn(10);
        when(pointHistoryRepository.sumUsedInPeriod(any(), any(), any())).thenReturn(5);
        when(pointHistoryRepository
                .findByUserUserIdAndPointExpiredDateBetweenAndRemainingPointGreaterThan(
                        any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        var summary = pointHistoryService.getMyPointSummary(1L);

        assertEquals(10, summary.getEarnedThisMonth());
    }
}
