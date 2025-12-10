package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
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
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointHistoryAdminController {

    private final PointHistoryService pointHistoryService;
    private static final String USER_ID_HEADER = "X-User-Id";

    // 1. (관리자) 특정 유저의 포인트 전체 이력 조회
    // GET /admin/points
    @GetMapping
    public ResponseEntity<Page<PointHistoryResponseDto>> getUserPointHistory(
//            @RequestHeader(USER_ID_HEADER) Long adminUserId, // == userId / adminUserId: 이 API를 호출한 관리자 ID
            @RequestParam Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<PointHistoryResponseDto> pointHistory = pointHistoryService.getMyPointHistory(userId, pageable);
        return ResponseEntity.ok(pointHistory);
    }

    // 2. (관리자) 특정 유저의 현재 보유 포인트 조회
    // GET /admin/points/current
    @GetMapping("/current")
    public ResponseEntity<CurrentPointResponseDto> getUserCurrentPoint(
//            @RequestHeader(USER_ID_HEADER) Long adminUserId,
            @RequestParam Long userId
    ) {
        CurrentPointResponseDto currentPoint = pointHistoryService.getMyCurrentPoint(userId);
        return ResponseEntity.ok(currentPoint);
    }

    // 3. (관리자) 수동 포인트 지급/차감
    // POST /admin/points/adjust
    @PostMapping("/adjust")
    public ResponseEntity<EarnPointResponseDto> adjustPointByAdmin(
//            @RequestHeader(USER_ID_HEADER) Long adminUserId,
            @Valid @RequestBody PointHistoryAdminAdjustRequestDto requestDto
    ) {
        // requestDto.getUserId() : 포인트 조정 대상 회원
        // adminUserId           : 이 조정을 실행한 관리자
        EarnPointResponseDto earnPoint = pointHistoryService.adjustPointByAdmin(requestDto);
        return ResponseEntity.ok(earnPoint);
    }

    // 4. (관리자) 포인트 "자동" 만료 처리
    // POST /users/me/points/expire
    @PostMapping("/expire")
    public ResponseEntity<Void> expirePoints(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        pointHistoryService.expirePoints(userId);
        return ResponseEntity.ok().build();
    }
}