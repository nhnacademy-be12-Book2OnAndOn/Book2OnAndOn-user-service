package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointSummaryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    // 1. 포인트 내역 조회 (적립/사용)
    // GET /users/me/points?type=EARN
    // GET /users/me/points?type=USE
    @GetMapping(params = "type")
    public ResponseEntity<Page<PointHistoryResponseDto>> getMyPointHistoryByType(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam("type") String type,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<PointHistoryResponseDto> pointHistory =
                pointHistoryService.getMyPointHistoryByType(userId, type, pageable);

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

    // 3. 7일 내 소멸 예정 포인트 조회
    // GET /users/me/points/expiring?days=7
    @GetMapping("/expiring")
    public ResponseEntity<ExpiringPointResponseDto> getExpiringPoints(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        ExpiringPointResponseDto expiringPoints = pointHistoryService.getExpiringPoints(userId, days);
        return ResponseEntity.ok().body(expiringPoints);
    }

    // 4. 포인트 내역 요약 (프론트)
    @GetMapping("/summary")
    public ResponseEntity<PointSummaryResponseDto> getSummary(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        PointSummaryResponseDto dto = pointHistoryService.getMyPointSummary(userId);
        return ResponseEntity.ok(dto);
    }

}