package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-setting")
@RequiredArgsConstructor
public class PointHistoryAdminController {

    private final PointHistoryService pointHistoryService;

    // 1. 포인트 내역 조회 (200) (적립(+), 사용(-))
    @GetMapping
    public Page<PointHistoryResponseDto> checkMyPointHistory(Long userId, Pageable pageable) {
        return pointHistoryService.getMyPointHistory(userId, pageable);
    }



}
