package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/points")
@RequiredArgsConstructor
public class PointHistoryUserController {

    private final PointHistoryService pointHistoryService;

    private static final String USER_ID_HEADER = "X-User-Id";

    // 1. 포인트 전체 내역 조회 (마이페이지)
    // GET /users/me/points
    @GetMapping
    public ResponseEntity<Page<PointHistoryResponseDto>> getMyPointHistory(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<PointHistoryResponseDto> pointHistory = pointHistoryService.getMyPointHistory(userId, pageable);
        return ResponseEntity.ok(pointHistory);
    }

    // 2. 현재 포인트 조회 (숫자만)
    // GET /users/me/points/current
    @GetMapping("/current")
    public ResponseEntity<CurrentPointResponseDto> getMyCurrentPoint(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        CurrentPointResponseDto currentPoint = pointHistoryService.getMyCurrentPoint(userId);
        return ResponseEntity.ok(currentPoint);
    }

    // 3-1. 회원가입 적립
    // POST /users/me/points/earn/signup
    @PostMapping("/earn/signup")
    public ResponseEntity<EarnPointResponseDto> earnSignupPoint(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        EarnPointResponseDto earnPoint = pointHistoryService.earnSignupPoint(userId);
        return ResponseEntity.ok(earnPoint);
    }

    // 3-2. 리뷰 작성 적립 (일반/사진)
    // POST /users/me/points/earn/review
//    @PostMapping("/earn/review")
//    public ResponseEntity<EarnPointResponseDto> earnReviewPoint(
//            @RequestHeader(USER_ID_HEADER) Long userId,
//            @Valid @RequestBody EarnReviewPointRequestDto dto
//    ) {
//        // "me" 보장: Body 안에 userId가 있다면 헤더와 일치하는지 검증
//        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
//            throw new UserIdMismatchException(userId);
//        }
//        dto.setUserId(userId);
//        EarnPointResponseDto earnPoint = pointHistoryService.earnReviewPoint(dto);
//        return ResponseEntity.ok(earnPoint);
//    }
    @PostMapping("/earn/review")
    public ResponseEntity<EarnPointResponseDto> earnReviewPoint(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody EarnReviewPointRequestDto dto
    ) {
        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청 userId와 인증된 사용자가 일치하지 않습니다.");
        }
        EarnPointResponseDto earnPoint = pointHistoryService.earnReviewPoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 3-3. 도서 결제 적립 (적립률)
    // POST /users/me/points/earn/order
    @PostMapping("/earn/order")
    public ResponseEntity<EarnPointResponseDto> earnOrderPoint(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody EarnOrderPointRequestDto dto
    ) {
        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청 userId와 인증된 사용자가 일치하지 않습니다.");
        }
        dto.setUserId(userId);
        EarnPointResponseDto earnPoint = pointHistoryService.earnOrderPoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 4. 포인트 사용
    // POST /users/me/points/use
    @PostMapping("/use")
    public ResponseEntity<EarnPointResponseDto> usePoint(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody UsePointRequestDto dto
    ) {
        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청 userId와 인증된 사용자가 일치하지 않습니다.");
        }
        dto.setUserId(userId);
        EarnPointResponseDto earnPoint = pointHistoryService.usePoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 5. 포인트 반환 (결제취소/반품)
    // POST /users/me/points/refund
    @PostMapping("/refund")
    public ResponseEntity<EarnPointResponseDto> refundPoint(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody RefundPointRequestDto dto
    ) {
        if (dto.getUserId() != null && !dto.getUserId().equals(userId)) {
            throw new IllegalArgumentException("요청 userId와 인증된 사용자가 일치하지 않습니다.");
        }
        EarnPointResponseDto earnPoint = pointHistoryService.refundPoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 6. 7일 내 소멸 예정 포인트 조회
    // GET /users/me/points/expiring?days=7
    @GetMapping("/expiring")
    public ResponseEntity<ExpiringPointResponseDto> getExpiringPoints(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        ExpiringPointResponseDto expiringPoints = pointHistoryService.getExpiringPoints(userId, days);
        return ResponseEntity.ok().body(expiringPoints);
    }
}