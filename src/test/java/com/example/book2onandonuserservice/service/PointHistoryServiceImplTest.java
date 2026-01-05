package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.service.PointHistoryServiceImpl;
import com.example.book2onandonuserservice.point.support.pointhistory.PointCalculationHelper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryMapper;
import com.example.book2onandonuserservice.point.support.pointhistory.PointHistoryValidator;
import com.example.book2onandonuserservice.point.support.pointhistory.UserReferenceLoader;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.MissingRequiredFieldException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDateTime;
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

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceImplTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UsersRepository usersRepository; // 생성자 주입상 필요
    @Mock
    private UserReferenceLoader userReferenceLoader;
    @Mock
    private PointCalculationHelper pointCalculationHelper;
    @Mock
    private PointHistoryMapper pointHistoryMapper;
    @Mock
    private PointHistoryValidator pointHistoryValidator;

    @InjectMocks
    private PointHistoryServiceImpl pointHistoryService;

    private Users user;
    private UserGrade grade;

    @BeforeEach
    void setup() {
        user = mock(Users.class);
        grade = mock(UserGrade.class);
    }

    // =========================================================
    // 조회
    // =========================================================
    @Test
    void getMyPointHistory_success() {
        Pageable pageable = PageRequest.of(0, 10);

        PointHistory h1 = mock(PointHistory.class);
        PointHistory h2 = mock(PointHistory.class);

        PointHistoryResponseDto dto1 = mock(PointHistoryResponseDto.class);
        PointHistoryResponseDto dto2 = mock(PointHistoryResponseDto.class);

        Page<PointHistory> page = new PageImpl<>(List.of(h1, h2));

        when(pointHistoryRepository.findAllByUserUserIdOrderByPointCreatedDateDesc(1L, pageable))
                .thenReturn(page);
        when(pointHistoryMapper.toDto(h1)).thenReturn(dto1);
        when(pointHistoryMapper.toDto(h2)).thenReturn(dto2);

        Page<PointHistoryResponseDto> result = pointHistoryService.getMyPointHistory(1L, pageable);

        assertEquals(2, result.getContent().size());
        verify(pointHistoryRepository).findAllByUserUserIdOrderByPointCreatedDateDesc(1L, pageable);
    }

    @Test
    void getMyPointHistoryByType_EARN() {
        Page<PointHistory> page = new PageImpl<>(List.of(mock(PointHistory.class)));

        when(pointHistoryRepository.findEarnedPoints(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(pointHistoryMapper.toDto(any(PointHistory.class)))
                .thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res = pointHistoryService.getMyPointHistoryByType(
                1L, "EARN", PageRequest.of(0, 10)
        );

        assertEquals(1, res.getTotalElements());
        verify(pointHistoryRepository).findEarnedPoints(eq(1L), any(Pageable.class));
    }

    @Test
    void getMyPointHistoryByType_USE() {
        Page<PointHistory> page = new PageImpl<>(List.of(mock(PointHistory.class)));

        when(pointHistoryRepository.findUsedPoints(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(pointHistoryMapper.toDto(any(PointHistory.class)))
                .thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res = pointHistoryService.getMyPointHistoryByType(
                1L, "USE", PageRequest.of(0, 10)
        );

        assertEquals(1, res.getTotalElements());
        verify(pointHistoryRepository).findUsedPoints(eq(1L), any(Pageable.class));
    }

    @Test
    void getMyPointHistoryByType_ELSE() {
        Page<PointHistory> page = new PageImpl<>(List.of(mock(PointHistory.class)));

        when(pointHistoryRepository.findAllByUserUserIdOrderByPointCreatedDateDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(pointHistoryMapper.toDto(any(PointHistory.class)))
                .thenReturn(mock(PointHistoryResponseDto.class));

        Page<PointHistoryResponseDto> res = pointHistoryService.getMyPointHistoryByType(
                1L, "ETC", PageRequest.of(0, 10)
        );

        assertEquals(1, res.getTotalElements());
        verify(pointHistoryRepository).findAllByUserUserIdOrderByPointCreatedDateDesc(eq(1L), any(Pageable.class));
    }

    @Test
    void getMyCurrentPoint_success() {
        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(150);

        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latest));

        CurrentPointResponseDto dto = pointHistoryService.getMyCurrentPoint(1L);

        assertEquals(150, dto.getCurrentPoint());
        verify(pointHistoryRepository).findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L);
    }

    // =========================================================
    // SIGNUP 적립
    // =========================================================
    @Test
    void earnSignupPoint_success() {
        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());
        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP)).thenReturn(false);

        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP)).thenReturn(50);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.SIGNUP),
                eq(50),
                eq(50),
                isNull(),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(mapped);

        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
        assertEquals(PointReason.SIGNUP, dto.getEarnReason());

        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void earnSignupPoint_noChange_returnsZeroAndDoesNotSave() {
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP)).thenReturn(false);
        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP)).thenReturn(0);

        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);

        assertEquals(0, dto.getChangedPoint());
        assertEquals(100, dto.getTotalPointAfter());
        assertEquals(PointReason.SIGNUP, dto.getEarnReason());

        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        verify(pointHistoryMapper, never()).toEarnEntity(any(), any(), anyInt(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void earnSignupPoint_duplicate_throws() {
        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());
        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP)).thenReturn(true);

        assertThrows(SignupPointAlreadyGrantedException.class,
                () -> pointHistoryService.earnSignupPoint(1L));
    }

    // =========================================================
    // REVIEW 적립
    // =========================================================
    @Test
    void earnReviewPoint_text_success() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(1L);
        req.setReviewId(101L);
        req.setHasImage(false);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());

        when(pointCalculationHelper.calculateByPolicyName("REVIEW_TEXT")).thenReturn(50);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REVIEW),
                eq(50),
                eq(50),
                isNull(),
                eq(101L),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(mapped);

        EarnPointResponseDto dto = pointHistoryService.earnReviewPoint(req);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
        assertEquals(PointReason.REVIEW, dto.getEarnReason());

        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void earnReviewPoint_photo_success() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(1L);
        req.setReviewId(101L);
        req.setHasImage(true);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());

        when(pointCalculationHelper.calculateByPolicyName("REVIEW_PHOTO")).thenReturn(50);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REVIEW),
                eq(50),
                eq(50),
                isNull(),
                eq(101L),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(mapped);

        EarnPointResponseDto dto = pointHistoryService.earnReviewPoint(req);

        assertEquals(50, dto.getChangedPoint());
        assertEquals(50, dto.getTotalPointAfter());
        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void earnReviewPoint_changeZero_returnsZeroAndNoSave() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(1L);
        req.setReviewId(10L);
        req.setHasImage(false);

        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(10L);
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(pointCalculationHelper.calculateByPolicyName("REVIEW_TEXT")).thenReturn(0);

        EarnPointResponseDto res = pointHistoryService.earnReviewPoint(req);

        assertEquals(0, res.getChangedPoint());
        assertEquals(100, res.getTotalPointAfter());
        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
    }

    @Test
    void earnReviewPoint_duplicate_throws() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(1L);
        req.setReviewId(101L);
        req.setHasImage(true);

        doThrow(new ReviewAlreadyRewardedException(101L))
                .when(pointHistoryValidator).validateReviewNotDuplicated(101L);

        assertThrows(ReviewAlreadyRewardedException.class,
                () -> pointHistoryService.earnReviewPoint(req));
    }

    @Test
    void earnReviewPoint_userIdNull_throws() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(null);
        req.setReviewId(101L);
        req.setHasImage(false);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.earnReviewPoint(req));
    }

    @Test
    void earnReviewPoint_reviewIdNull_throws() {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto();
        req.setUserId(1L);
        req.setReviewId(null);
        req.setHasImage(false);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.earnReviewPoint(req));
    }

    // =========================================================
    // ORDER 적립 (등급 적립률 사용)
    // =========================================================
    @Test
    void earnOrderPoint_success_usesGradeRate() {
        EarnOrderPointRequestDto req = new EarnOrderPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setPureAmount(1000);

        doNothing().when(pointHistoryValidator).validateOrderEarnNotDuplicated(201L);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(1000), anyString());

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());

        when(user.getUserGrade()).thenReturn(grade);
        // 등급 적립률: 0.1 => round(1000 * 0.1) = 100
        when(grade.getUserPointAddRate()).thenReturn(0.1);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.ORDER),
                eq(100),
                eq(100),
                eq(201L),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(mapped);

        EarnPointResponseDto dto = pointHistoryService.earnOrderPoint(req);

        assertEquals(100, dto.getChangedPoint());
        assertEquals(100, dto.getTotalPointAfter());
        assertEquals(PointReason.ORDER, dto.getEarnReason());

        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void earnOrderPoint_changeZero_returnsZeroAndNoSave() {
        // given
        EarnOrderPointRequestDto req = new EarnOrderPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(100L);
        req.setPureAmount(1);

        doNothing().when(pointHistoryValidator).validateOrderEarnNotDuplicated(100L);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(1), anyString());

        // latest total = 50
        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(50);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        // user + grade 연결 (NPE 방지)
        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(user.getUserGrade()).thenReturn(grade);

        // 등급 적립률이 너무 낮아 0으로 반올림
        when(grade.getUserPointAddRate()).thenReturn(0.001);

        // when
        EarnPointResponseDto res = pointHistoryService.earnOrderPoint(req);

        // then
        assertEquals(0, res.getChangedPoint());
        assertEquals(50, res.getTotalPointAfter());
        assertEquals(PointReason.ORDER, res.getEarnReason());

        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        verify(pointHistoryRepository, never()).saveAll(anyList());
    }

    @Test
    void earnOrderPoint_userIdNull_throws() {
        EarnOrderPointRequestDto req = new EarnOrderPointRequestDto();
        req.setUserId(null);
        req.setOrderId(201L);
        req.setPureAmount(1000);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.earnOrderPoint(req));
    }

    @Test
    void earnOrderPoint_orderIdNull_throws() {
        EarnOrderPointRequestDto req = new EarnOrderPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(null);
        req.setPureAmount(1000);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.earnOrderPoint(req));
    }

    @Test
    void earnOrderPoint_pureAmountNull_throws() {
        EarnOrderPointRequestDto req = new EarnOrderPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setPureAmount(null);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.earnOrderPoint(req));
    }

    // =========================================================
    // USE (FIFO)
    // =========================================================
    @Test
    void usePoint_success_fifo() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setUseAmount(60);

        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(60), anyString());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);

        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(50);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(r1, r2));

        PointHistory savedUse = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.USE),
                eq(-60),
                eq(40),
                eq(201L),
                isNull(),
                isNull()
        )).thenReturn(savedUse);

        EarnPointResponseDto dto = pointHistoryService.usePoint(req);

        assertEquals(-60, dto.getChangedPoint());
        assertEquals(40, dto.getTotalPointAfter());
        assertEquals(0, r1.getRemainingPoint());
        assertEquals(20, r2.getRemainingPoint());

        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(savedUse);
    }

    @Test
    void usePoint_notEnough_throws() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setUseAmount(100);

        doNothing().when(pointHistoryValidator).validatePositiveAmount(anyInt(), anyString());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(50);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        assertThrows(InsufficientPointException.class,
                () -> pointHistoryService.usePoint(req));
    }

    @Test
    void usePoint_integrityException_whenEarnRowsInsufficient() {
        UsePointRequestDto dto = new UsePointRequestDto();
        dto.setUserId(1L);
        dto.setOrderId(200L);
        dto.setUseAmount(30);

        doNothing().when(pointHistoryValidator).validatePositiveAmount(anyInt(), anyString());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(anyLong());

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(30);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of()); // FIFO rows 없음 -> remainToUse 남음

        assertThrows(PointBalanceIntegrityException.class,
                () -> pointHistoryService.usePoint(dto));
    }

    @Test
    void usePoint_breaksEarly_whenRemainToUseBecomesZero() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setUseAmount(10);

        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(10), anyString());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(999);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(50);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(r1, r2));

        PointHistory savedUse = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.USE),
                eq(-10),
                eq(90),
                eq(201L),
                isNull(),
                isNull()
        )).thenReturn(savedUse);

        pointHistoryService.usePoint(req);

        assertEquals(989, r1.getRemainingPoint());
        assertEquals(50, r2.getRemainingPoint());
    }

    @Test
    void usePoint_skipsRow_whenRemainingNullOrZero() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setUseAmount(10);

        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(10), anyString());
        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory r0 = new PointHistory();
        r0.setRemainingPoint(null);
        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(0);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(50);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(r0, r1, r2));

        PointHistory savedUse = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.USE),
                eq(-10),
                eq(90),
                eq(201L),
                isNull(),
                isNull()
        )).thenReturn(savedUse);

        pointHistoryService.usePoint(req);

        assertEquals(40, r2.getRemainingPoint());
    }

    @Test
    void usePoint_userIdNull_throws() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(null);
        req.setOrderId(201L);
        req.setUseAmount(10);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.usePoint(req));
    }

    @Test
    void usePoint_orderIdNull_throws() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(null);
        req.setUseAmount(10);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.usePoint(req));
    }

    @Test
    void usePoint_useAmountNull_throws() {
        UsePointRequestDto req = new UsePointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setUseAmount(null);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.usePoint(req));
    }

    // =========================================================
    // useCancel (취소)
    // =========================================================
    @Test
    void useCancel_allIdempotent_returnsZero() {
        Long orderId = 10L;
        Long userId = 1L;

        when(pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.REFUND))
                .thenReturn(true);
        when(pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.ORDER_RECLAIM))
                .thenReturn(true);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(userId)).thenReturn(Optional.of(latest));

        EarnPointResponseDto res = pointHistoryService.useCancel(orderId, userId);

        assertEquals(0, res.getChangedPoint());
        assertEquals(100, res.getTotalPointAfter());
        assertEquals(PointReason.REFUND, res.getEarnReason());
    }

    @Test
    void useCancel_success_rollBackUsed_and_reclaimOrderRemaining() {
        Long orderId = 10L;
        Long userId = 1L;

        when(pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.REFUND))
                .thenReturn(false);
        when(pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.ORDER_RECLAIM))
                .thenReturn(false);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(userId)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(userId)).thenReturn(user);

        when(pointHistoryRepository.sumUsedPointByOrder(orderId, PointReason.USE)).thenReturn(50);

        PointHistory rollback = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REFUND),
                eq(50),
                eq(150),
                eq(orderId),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(rollback);

        // helper 내부 재체크도 false로
        when(pointHistoryRepository.existsByOrderIdAndPointReasonAndRefundIdIsNull(orderId, PointReason.ORDER_RECLAIM))
                .thenReturn(false);

        PointHistory orderEarn1 = new PointHistory();
        orderEarn1.setRemainingPoint(30);
        PointHistory orderEarn2 = new PointHistory();
        orderEarn2.setRemainingPoint(20);

        when(pointHistoryRepository.findByOrderIdAndPointReason(orderId, PointReason.ORDER))
                .thenReturn(List.of(orderEarn1, orderEarn2));

        PointHistory reclaimHistory = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.ORDER_RECLAIM),
                eq(-50),
                eq(100),
                eq(orderId),
                isNull(),
                isNull()
        )).thenReturn(reclaimHistory);

        EarnPointResponseDto res = pointHistoryService.useCancel(orderId, userId);

        assertEquals(0, res.getChangedPoint());
        assertEquals(100, res.getTotalPointAfter());
        assertEquals(PointReason.REFUND, res.getEarnReason());

        verify(pointHistoryRepository).save(rollback);
        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(reclaimHistory);

        assertEquals(0, orderEarn1.getRemainingPoint());
        assertEquals(0, orderEarn2.getRemainingPoint());
    }

    @Test
    void useCancel_orderIdNull_throws() {
        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.useCancel(null, 1L));
    }

    @Test
    void useCancel_userIdNull_throws() {
        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.useCancel(10L, null));
    }

    // =========================================================
    // refundPoint (반품)
    // =========================================================
    @Test
    void refundPoint_success_usedRestore_and_returnAmount_and_reclaimOrderRemaining() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setRefundId(301L);
        req.setUsedPoint(20);
        req.setRefundAmount(30);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(301L);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(30), anyString());

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(0);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(pointHistoryRepository.sumUsedPointByOrder(201L, PointReason.USE)).thenReturn(20);

        PointHistory restore = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REFUND),
                eq(20),
                eq(20),
                eq(201L),
                isNull(),
                eq(301L),
                any(LocalDateTime.class)
        )).thenReturn(restore);

        PointHistory refundAmount = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.REFUND),
                eq(30),
                eq(50),
                eq(201L),
                isNull(),
                eq(301L),
                isNull()
        )).thenReturn(refundAmount);

        when(pointHistoryRepository.existsByOrderIdAndRefundIdAndPointReason(201L, 301L, PointReason.ORDER_RECLAIM))
                .thenReturn(false);

        PointHistory orderEarn = new PointHistory();
        orderEarn.setRemainingPoint(10);
        when(pointHistoryRepository.findByOrderIdAndPointReason(201L, PointReason.ORDER))
                .thenReturn(List.of(orderEarn));

        PointHistory reclaim = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.ORDER_RECLAIM),
                eq(-10),
                eq(40),
                eq(201L),
                isNull(),
                eq(301L)
        )).thenReturn(reclaim);

        EarnPointResponseDto dto = pointHistoryService.refundPoint(req);

        assertEquals(40, dto.getChangedPoint());
        assertEquals(40, dto.getTotalPointAfter());
        assertEquals(PointReason.REFUND, dto.getEarnReason());

        verify(pointHistoryRepository).save(restore);
        verify(pointHistoryRepository).save(refundAmount);

        assertEquals(0, orderEarn.getRemainingPoint());
        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(reclaim);
    }

    @Test
    void refundPoint_usedPointExceed_throws() {
        RefundPointRequestDto dto = new RefundPointRequestDto();
        dto.setUserId(1L);
        dto.setOrderId(1L);
        dto.setRefundId(1L);
        dto.setUsedPoint(50);
        dto.setRefundAmount(0);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(anyLong());
        when(userReferenceLoader.getReference(anyLong())).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(0);
        when(pointHistoryRepository.findLatestForUpdateOne(anyLong())).thenReturn(Optional.of(latest));

        when(pointHistoryRepository.sumUsedPointByOrder(anyLong(), eq(PointReason.USE)))
                .thenReturn(10);

        assertThrows(RefundPointRangeExceededException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    @Test
    void refundPoint_noUsedPointButUsedPointRequested_throws() {
        RefundPointRequestDto dto = new RefundPointRequestDto();
        dto.setUserId(1L);
        dto.setOrderId(10L);
        dto.setRefundId(20L);
        dto.setUsedPoint(1);
        dto.setRefundAmount(0);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(20L);
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(0);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(pointHistoryRepository.sumUsedPointByOrder(10L, PointReason.USE)).thenReturn(0);

        assertThrows(InvalidRefundPointException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    @Test
    void refundPoint_negativeReturnAmount_throws() {
        RefundPointRequestDto dto = new RefundPointRequestDto();
        dto.setUserId(1L);
        dto.setOrderId(10L);
        dto.setRefundId(20L);
        dto.setUsedPoint(10);
        dto.setRefundAmount(-5);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(20L);

        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRefundPointException.class,
                () -> pointHistoryService.refundPoint(dto));
    }

    @Test
    void refundPoint_noChange_returnsZero() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setRefundId(301L);
        req.setUsedPoint(0);
        req.setRefundAmount(0);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(301L);
        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(123);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        EarnPointResponseDto res = pointHistoryService.refundPoint(req);

        assertEquals(0, res.getChangedPoint());
        assertEquals(123, res.getTotalPointAfter());
        assertEquals(PointReason.REFUND, res.getEarnReason());
    }

    @Test
    void refundPoint_negativeUsedPoint_throws() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setRefundId(301L);
        req.setUsedPoint(-1);
        req.setRefundAmount(0);

        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(301L);
        when(userReferenceLoader.getReference(1L)).thenReturn(user);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRefundPointException.class, () -> pointHistoryService.refundPoint(req));
    }

    @Test
    void refundPoint_userIdNull_throws() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(null);
        req.setOrderId(201L);
        req.setRefundId(301L);
        req.setUsedPoint(0);
        req.setRefundAmount(0);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.refundPoint(req));
    }

    @Test
    void refundPoint_orderIdNull_throws() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(null);
        req.setRefundId(301L);
        req.setUsedPoint(0);
        req.setRefundAmount(0);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.refundPoint(req));
    }

    @Test
    void refundPoint_refundIdNull_throws() {
        RefundPointRequestDto req = new RefundPointRequestDto();
        req.setUserId(1L);
        req.setOrderId(201L);
        req.setRefundId(null);
        req.setUsedPoint(0);
        req.setRefundAmount(0);

        assertThrows(MissingRequiredFieldException.class, () -> pointHistoryService.refundPoint(req));
    }

    // =========================================================
    // expirePoints
    // =========================================================
    @Test
    void expirePoints_success() {
        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(20);

        when(pointHistoryValidator.getExpiredEarnRows(1L))
                .thenReturn(List.of(r1, r2));

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.EXPIRE),
                eq(-50),
                eq(50),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mapped);

        pointHistoryService.expirePoints(1L);

        assertEquals(0, r1.getRemainingPoint());
        assertEquals(0, r2.getRemainingPoint());

        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void expirePoints_empty_noSave() {
        when(pointHistoryValidator.getExpiredEarnRows(1L)).thenReturn(List.of());

        pointHistoryService.expirePoints(1L);

        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        verify(pointHistoryRepository, never()).saveAll(anyList());
    }

    // =========================================================
    // expireAllPointsForWithdraw
    // =========================================================
    @Test
    void expireAllPointsForWithdraw_success() {
        Long userId = 1L;

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(userId))
                .thenReturn(Optional.of(latest));

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(20);

        when(pointHistoryValidator.getAllRemainingEarnRows(userId))
                .thenReturn(List.of(r1, r2));

        when(userReferenceLoader.getReference(userId)).thenReturn(user);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.WITHDRAW),
                eq(-50),
                eq(50),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mapped);

        pointHistoryService.expireAllPointsForWithdraw(userId);

        assertEquals(0, r1.getRemainingPoint());
        assertEquals(0, r2.getRemainingPoint());

        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void expireAllPointsForWithdraw_rowsEmpty_returnsEarly() {
        Long userId = 1L;

        when(pointHistoryRepository.findLatestForUpdateOne(userId))
                .thenReturn(Optional.empty());

        when(pointHistoryValidator.getAllRemainingEarnRows(userId))
                .thenReturn(List.of());

        pointHistoryService.expireAllPointsForWithdraw(userId);

        verify(pointHistoryRepository, never()).saveAll(anyList());
        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        verify(userReferenceLoader, never()).getReference(anyLong());
        verify(pointHistoryMapper, never()).toUseOrDeductEntity(any(), any(), anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    void expireAllPointsForWithdraw_expireTotalZero_returnsEarly() {
        Long userId = 1L;

        when(pointHistoryRepository.findLatestForUpdateOne(userId))
                .thenReturn(Optional.empty());

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(null);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(0);

        when(pointHistoryValidator.getAllRemainingEarnRows(userId))
                .thenReturn(List.of(r1, r2));

        pointHistoryService.expireAllPointsForWithdraw(userId);

        verify(pointHistoryRepository, never()).saveAll(anyList());
        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
        verify(userReferenceLoader, never()).getReference(anyLong());
        verify(pointHistoryMapper, never()).toUseOrDeductEntity(any(), any(), anyInt(), anyInt(), any(), any(), any());
    }

    // =========================================================
    // getExpiringPoints
    // =========================================================
    @Test
    void getExpiringPoints_empty() {
        when(pointHistoryRepository.findSoonExpiringPoints(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        ExpiringPointResponseDto res = pointHistoryService.getExpiringPoints(1L, 7);

        assertEquals(0, res.getExpiringAmount());
    }

    // =========================================================
    // adjustPointByAdmin
    // =========================================================
    @Test
    void adjustPointByAdmin_success_add() {
        PointHistoryAdminAdjustRequestDto dto = new PointHistoryAdminAdjustRequestDto(1L, 30, "메모");

        doNothing().when(pointHistoryValidator).validateAdjustAmountNotZero(30);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(30), anyString());

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toEarnEntity(
                eq(user),
                eq(PointReason.ADMIN_ADJUST),
                eq(30),
                eq(130),
                isNull(),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        )).thenReturn(mapped);

        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);

        assertEquals(30, res.getChangedPoint());
        assertEquals(130, res.getTotalPointAfter());
        assertEquals(PointReason.ADMIN_ADJUST, res.getEarnReason());

        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void adjustPointByAdmin_amountZero_throws() {
        PointHistoryAdminAdjustRequestDto dto = new PointHistoryAdminAdjustRequestDto(1L, 0, "메모");

        doThrow(new InvalidAdminAdjustPointException("조정 포인트는 0일 수 없습니다."))
                .when(pointHistoryValidator).validateAdjustAmountNotZero(0);

        assertThrows(InvalidAdminAdjustPointException.class,
                () -> pointHistoryService.adjustPointByAdmin(dto));
    }

    @Test
    void adjustPointByAdmin_negativeBalance_throws() {
        PointHistoryAdminAdjustRequestDto dto = new PointHistoryAdminAdjustRequestDto(1L, -20, "차감");

        doNothing().when(pointHistoryValidator).validateAdjustAmountNotZero(-20);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(20), anyString());

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(10);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        assertThrows(AdminAdjustPointNegativeBalanceException.class,
                () -> pointHistoryService.adjustPointByAdmin(dto));
    }

    @Test
    void adjustPointByAdmin_success_deduct_fifo() {
        PointHistoryAdminAdjustRequestDto dto = new PointHistoryAdminAdjustRequestDto(1L, -50, "차감");

        doNothing().when(pointHistoryValidator).validateAdjustAmountNotZero(-50);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(50), anyString());

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(30);
        PointHistory r2 = new PointHistory();
        r2.setRemainingPoint(30);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(r1, r2));

        PointHistory mapped = mock(PointHistory.class);
        when(pointHistoryMapper.toUseOrDeductEntity(
                eq(user),
                eq(PointReason.ADMIN_ADJUST),
                eq(-50),
                eq(50),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(mapped);

        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);

        assertEquals(-50, res.getChangedPoint());
        assertEquals(50, res.getTotalPointAfter());
        assertEquals(PointReason.ADMIN_ADJUST, res.getEarnReason());

        assertEquals(0, r1.getRemainingPoint());
        assertEquals(10, r2.getRemainingPoint());

        verify(pointHistoryRepository).saveAll(anyList());
        verify(pointHistoryRepository).save(mapped);
    }

    @Test
    void adjustPointByAdmin_integrityException_whenEarnRowsInsufficient() {
        PointHistoryAdminAdjustRequestDto dto = new PointHistoryAdminAdjustRequestDto(1L, -50, "차감");

        doNothing().when(pointHistoryValidator).validateAdjustAmountNotZero(-50);
        doNothing().when(pointHistoryValidator).validatePositiveAmount(eq(50), anyString());

        PointHistory latest = mock(PointHistory.class);
        when(latest.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findLatestForUpdateOne(1L)).thenReturn(Optional.of(latest));

        when(userReferenceLoader.getReference(1L)).thenReturn(user);

        PointHistory r1 = new PointHistory();
        r1.setRemainingPoint(10);

        when(pointHistoryRepository.findPointsForUsage(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(r1));

        assertThrows(PointBalanceIntegrityException.class,
                () -> pointHistoryService.adjustPointByAdmin(dto));
    }

    // =========================================================
    // getMyPointSummary
    // =========================================================
    @Test
    void getMyPointSummary_cover() {
        PointHistory latestRead = mock(PointHistory.class);
        when(latestRead.getTotalPoints()).thenReturn(100);
        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
                .thenReturn(Optional.of(latestRead));

        when(pointHistoryRepository.sumEarnedInPeriod(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10);
        when(pointHistoryRepository.sumUsedInPeriod(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5);

        when(pointHistoryRepository.findSoonExpiringPoints(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        var summary = pointHistoryService.getMyPointSummary(1L);

        assertEquals(100, summary.getTotalPoint());
        assertEquals(10, summary.getEarnedThisMonth());
        assertEquals(5, summary.getUsedThisMonth());
        assertEquals(0, summary.getExpiringSoon());
    }
}
