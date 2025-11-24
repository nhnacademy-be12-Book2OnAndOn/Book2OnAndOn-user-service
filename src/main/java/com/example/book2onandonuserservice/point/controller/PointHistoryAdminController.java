package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.point.support.AdminAuthorization;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final AdminAuthorization adminAuthorization;

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_ROLE_HEADER = "X-USER-ROLE";

    // 1. (관리자) 특정 유저의 포인트 전체 이력 조회
    // GET /admin/points?userId=1
    @GetMapping
    public Page<PointHistoryResponseDto> getUserPointHistory(
            @RequestHeader(USER_ID_HEADER) Long adminUserId, // adminUserId: 이 API를 호출한 관리자 ID
            @RequestHeader(USER_ROLE_HEADER) String role, // userId: 조회 대상 회원 ID
            @RequestParam Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        adminAuthorization.requirePointAdmin(role);
        return pointHistoryService.getMyPointHistory(userId, pageable);
    }

    // 2. (관리자) 특정 유저의 현재 보유 포인트 조회
    // GET /admin/points/current?userId=1
    @GetMapping("/current")
    public CurrentPointResponseDto getUserCurrentPoint(
            @RequestHeader(USER_ID_HEADER) Long adminUserId,
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestParam Long userId
    ) {
        adminAuthorization.requirePointAdmin(role);
        return pointHistoryService.getMyCurrentPoint(userId);
    }

    // 3. (관리자) 수동 포인트 지급/차감
    // POST /admin/points/adjust
    @PostMapping("/adjust")
    public EarnPointResponseDto adjustPointByAdmin(
            @RequestHeader(USER_ID_HEADER) Long adminUserId,
            @RequestHeader(USER_ROLE_HEADER) String role,
            @Valid @RequestBody PointHistoryAdminAdjustRequestDto requestDto
    ) {
        adminAuthorization.requirePointAdmin(role);
        // requestDto.getUserId() : 포인트 조정 대상 회원
        // adminUserId           : 이 조정을 실행한 관리자
        return pointHistoryService.adjustPointByAdmin(requestDto);
    }
}