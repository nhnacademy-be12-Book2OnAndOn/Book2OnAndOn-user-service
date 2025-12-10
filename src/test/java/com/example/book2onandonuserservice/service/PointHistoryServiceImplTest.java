//package com.example.book2onandonuserservice.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.ArgumentMatchers.isNull;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
//import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
//import com.example.book2onandonuserservice.point.domain.entity.PointReason;
//import com.example.book2onandonuserservice.point.domain.entity.PointReviewType;
//import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
//import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
//import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
//import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
//import com.example.book2onandonuserservice.point.service.PointHistoryServiceImpl;
//import com.example.book2onandonuserservice.point.support.pointHistory.PointCalculationHelper;
//import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryMapper;
//import com.example.book2onandonuserservice.point.support.pointHistory.PointHistoryValidator;
//import com.example.book2onandonuserservice.point.support.pointHistory.UserReferenceLoader;
//import com.example.book2onandonuserservice.user.domain.entity.Users;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//class PointHistoryServiceImplTest {
//
//    @Mock
//    private PointHistoryRepository pointHistoryRepository;
//    @Mock
//    private PointHistoryMapper pointHistoryMapper;
//    @Mock
//    private PointHistoryValidator pointHistoryValidator;
//    @Mock
//    private UserReferenceLoader userReferenceLoader;
//    @Mock
//    private PointCalculationHelper pointCalculationHelper;
//
//    @InjectMocks
//    private PointHistoryServiceImpl pointHistoryService;
//
//    private Users user;
//
//    @BeforeEach
//    void setup() {
//        user = new Users();
//        ReflectionTestUtils.setField(user, "userId", 1L);
//    }
//
//    // 조회 테스트
//    @Test
//    void getMyPointHistory_success() {
//        Pageable pageable = PageRequest.of(0, 10);
//
//        PointHistory h1 = mock(PointHistory.class);
//        PointHistory h2 = mock(PointHistory.class);
//
//        PointHistoryResponseDto dto1 = mock(PointHistoryResponseDto.class);
//        PointHistoryResponseDto dto2 = mock(PointHistoryResponseDto.class);
//
//        Page<PointHistory> page = new PageImpl<>(List.of(h1, h2));
//
//        when(pointHistoryRepository
//                .findAllByUserUserIdOrderByPointCreatedDateDesc(1L, pageable))
//                .thenReturn(page);
//
//        when(pointHistoryMapper.toDto(h1)).thenReturn(dto1);
//        when(pointHistoryMapper.toDto(h2)).thenReturn(dto2);
//
//        Page<PointHistoryResponseDto> result =
//                pointHistoryService.getMyPointHistory(1L, pageable);
//
//        assertEquals(2, result.getContent().size());
//    }
//
//    @Test
//    void getMyCurrentPoint_success() {
//        PointHistory history = new PointHistory();
//        history.setTotalPoints(150);
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(history));
//
//        CurrentPointResponseDto dto = pointHistoryService.getMyCurrentPoint(1L);
//
//        assertEquals(150, dto.getCurrentPoint());
//    }
//
//    // SIGNUP 적립
//
//    @Test
//    void earnSignupPoint_success() {
//        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
//                .thenReturn(false);
//
//        when(pointHistoryRepository
//                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.empty());
//
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP, null))
//                .thenReturn(50);
//
//        PointHistory savedEntity = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(
//                eq(user), eq(PointReason.SIGNUP),
//                eq(50), eq(50),
//                isNull(), isNull(), isNull(), any()
//        )).thenReturn(savedEntity);
//
//        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);
//
//        assertEquals(50, dto.getChangedPoint());
//        assertEquals(50, dto.getTotalPointAfter());
//    }
//
//    @Test
//    void earnSignupPoint_noChange() {
//        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
//                .thenReturn(false);
//
//        when(pointHistoryRepository
//                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(new PointHistory()));
//
//        when(pointCalculationHelper.calculateByReason(PointReason.SIGNUP, null))
//                .thenReturn(0);
//
//        EarnPointResponseDto dto = pointHistoryService.earnSignupPoint(1L);
//
//        assertEquals(0, dto.getChangedPoint());
//    }
//
//    @Test
//    void earnSignupPoint_duplicate_throws() {
//        when(pointHistoryRepository.existsByUserUserIdAndPointReason(1L, PointReason.SIGNUP))
//                .thenReturn(true);
//
//        assertThrows(SignupPointAlreadyGrantedException.class,
//                () -> pointHistoryService.earnSignupPoint(1L));
//    }
//
//    // REVIEW 적립
//
//    @Test
//    void earnReviewPoint_text_success() {
//        EarnReviewPointRequestDto req =
//                new EarnReviewPointRequestDto(1L, 101L, 201L, PointReviewType.TEXT);
//
//        doNothing().when(pointHistoryValidator).validateReviewNotDuplicated(101L);
//
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.empty());
//
//        when(pointCalculationHelper.calculateByPolicyName("REVIEW_TEXT", null))
//                .thenReturn(50);
//
//        PointHistory entity = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(
//                eq(user), eq(PointReason.REVIEW),
//                eq(50), eq(50),
//                eq(201L), eq(101L), isNull(), any()
//        )).thenReturn(entity);
//
//        EarnPointResponseDto dto = pointHistoryService.earnReviewPoint(req);
//
//        assertEquals(50, dto.getChangedPoint());
//    }
//
//    @Test
//    void earnReviewPoint_duplicate_throws() {
//        EarnReviewPointRequestDto req =
//                new EarnReviewPointRequestDto(1L, 101L, 201L, PointReviewType.TEXT);
//
//        doThrow(new ReviewAlreadyRewardedException(101L))
//                .when(pointHistoryValidator).validateReviewNotDuplicated(101L);
//
//        assertThrows(ReviewAlreadyRewardedException.class,
//                () -> pointHistoryService.earnReviewPoint(req));
//    }
//
//    // ORDER 적립
//    @Test
//    void earnOrderPoint_success() {
//        EarnOrderPointRequestDto req =
//                new EarnOrderPointRequestDto(1L, 201L, 1000);
//
//        doNothing().when(pointHistoryValidator).validateOrderEarnNotDuplicated(201L);
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.empty());
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//        when(pointCalculationHelper.calculateByReason(PointReason.ORDER, 1000))
//                .thenReturn(50);
//
//        PointHistory entity = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(
//                eq(user), eq(PointReason.ORDER),
//                eq(50), eq(50),
//                eq(201L), isNull(), isNull(), any()
//        )).thenReturn(entity);
//
//        EarnPointResponseDto dto = pointHistoryService.earnOrderPoint(req);
//
//        assertEquals(50, dto.getChangedPoint());
//    }
//
//    // GRADE 적립
//    @Test
//    void earnGradePoint_success() {
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.empty());
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        PointHistory entity = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(
//                any(), eq(PointReason.ORDER),
//                eq(100), eq(100),
//                isNull(), isNull(), isNull(), any()
//        )).thenReturn(entity);
//
//        pointHistoryService.earnGradePoint(1L, 1000, 0.1);
//
//        verify(pointHistoryRepository).save(entity);
//    }
//
//    // 포인트 사용
//
//
//    @Test
//    void usePoint_success_fifo() {
//
//        UsePointRequestDto req = new UsePointRequestDto(1L, 201L, 60, 100);
//
//        doNothing().when(pointHistoryValidator).validatePointRange(60, 100);
//        doNothing().when(pointHistoryValidator).validatePositiveAmount(60, "사용 포인트는 0보다 커야 합니다.");
//        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);
//
//        PointHistory latest = new PointHistory();
//        latest.setTotalPoints(100);
//
//        when(pointHistoryRepository
//                .findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(latest));
//
//        PointHistory r1 = new PointHistory();
//        r1.setRemainingPoint(30);
//        r1.setPointHistoryChange(30);
//
//        PointHistory r2 = new PointHistory();
//        r2.setRemainingPoint(50);
//        r2.setPointHistoryChange(50);
//
//        when(pointHistoryRepository
//                .findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
//                        1L, 0, 0
//                )).thenReturn(List.of(r1, r2));
//
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        PointHistory savedUse = new PointHistory();
//
//        when(pointHistoryMapper.toUseOrDeductEntity(
//                eq(user), eq(PointReason.USE),
//                eq(-60), anyInt(),
//                eq(201L), isNull(), isNull()
//        )).thenReturn(savedUse);
//
//        EarnPointResponseDto dto = pointHistoryService.usePoint(req);
//
//        assertEquals(-60, dto.getChangedPoint());
//        assertEquals(0, r1.getRemainingPoint());
//        assertEquals(20, r2.getRemainingPoint());
//        verify(pointHistoryRepository).save(savedUse);
//    }
//
//
//    @Test
//    void usePoint_notEnough_throws() {
//        UsePointRequestDto req = new UsePointRequestDto(1L, 201L, 100, 200);
//
//        doNothing().when(pointHistoryValidator).validatePointRange(100, 200);
//        doNothing().when(pointHistoryValidator).validateUseNotDuplicated(201L);
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(new PointHistory()));
//
//        assertThrows(InsufficientPointException.class,
//                () -> pointHistoryService.usePoint(req));
//    }
//
//    // 환불
//
//    @Test
//    void refundPoint_success() {
//        RefundPointRequestDto req =
//                new RefundPointRequestDto(1L, 201L, 301L, 20, 30);
//
//        doNothing().when(pointHistoryValidator).validateReturnNotDuplicated(301L);
//
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(new PointHistory()));
//
//        when(pointHistoryRepository.findByOrderId(201L))
//                .thenReturn(List.of());
//
//        PointHistory saved1 = new PointHistory();
//        PointHistory saved2 = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(any(), eq(PointReason.REFUND),
//                eq(20), anyInt(), eq(201L), isNull(), eq(301L), any()
//        )).thenReturn(saved1);
//
//        when(pointHistoryMapper.toEarnEntity(any(), eq(PointReason.REFUND),
//                eq(30), anyInt(), eq(201L), isNull(), eq(301L), any()
//        )).thenReturn(saved2);
//
//        pointHistoryService.refundPoint(req);
//
//        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class));
//    }
//
//    // 만료
//    @Test
//    void expirePoints_success() {
//
//        PointHistory r1 = new PointHistory();
//        r1.setRemainingPoint(30);
//        r1.setPointHistoryChange(30);
//
//        PointHistory r2 = new PointHistory();
//        r2.setRemainingPoint(20);
//        r2.setPointHistoryChange(20);
//
//        when(pointHistoryValidator.getExpiredEarnRows(1L))
//                .thenReturn(List.of(r1, r2));
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(new PointHistory()));
//
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        PointHistory savedExpire = new PointHistory();
//
//        when(pointHistoryMapper.toUseOrDeductEntity(eq(user), eq(PointReason.EXPIRE),
//                eq(-50), anyInt(), isNull(), isNull(), isNull()
//        )).thenReturn(savedExpire);
//
//        pointHistoryService.expirePoints(1L);
//
//        verify(pointHistoryRepository).save(savedExpire);
//        assertEquals(0, r1.getRemainingPoint());
//        assertEquals(0, r2.getRemainingPoint());
//    }
//
//    // 관리자 조정
//    @Test
//    void adjustPointByAdmin_success_add() {
//        PointHistoryAdminAdjustRequestDto dto =
//                new PointHistoryAdminAdjustRequestDto(1L, 30, "메모");
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(new PointHistory()));
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        PointHistory saved = new PointHistory();
//
//        when(pointHistoryMapper.toEarnEntity(eq(user), eq(PointReason.ADMIN_ADJUST),
//                eq(30), anyInt(), isNull(), isNull(), isNull(), any()
//        )).thenReturn(saved);
//
//        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);
//
//        verify(pointHistoryRepository).save(saved);
//        assertEquals(30, res.getChangedPoint());
//    }
//
//    @Test
//    void adjustPointByAdmin_success_deduct() {
//        PointHistoryAdminAdjustRequestDto dto =
//                new PointHistoryAdminAdjustRequestDto(1L, -20, "메모");
//
//        PointHistory prev = new PointHistory();
//        prev.setTotalPoints(50);
//
//        when(pointHistoryRepository.findTop1ByUserUserIdOrderByPointCreatedDateDesc(1L))
//                .thenReturn(Optional.of(prev));
//        when(userReferenceLoader.getReference(1L)).thenReturn(user);
//
//        PointHistory saved = new PointHistory();
//
//        when(pointHistoryMapper.toUseOrDeductEntity(eq(user), eq(PointReason.ADMIN_ADJUST),
//                eq(-20), anyInt(), isNull(), isNull(), isNull()
//        )).thenReturn(saved);
//
//        EarnPointResponseDto res = pointHistoryService.adjustPointByAdmin(dto);
//
//        verify(pointHistoryRepository).save(saved);
//        assertEquals(-20, res.getChangedPoint());
//    }
//}
