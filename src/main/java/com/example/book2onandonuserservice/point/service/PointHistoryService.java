package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointHistoryService {

    // 1. 포인트 전체 내역 조회 (마이페이지)
    Page<PointHistoryResponseDto> getMyPointHistory(Long userId, Pageable pageable);

    // 2. 현재 보유 포인트 조회 (숫자만)
    CurrentPointResponseDto getMyCurrentPoint(Long userId);

    // 3-1. 회원가입 적립
    EarnPointResponseDto earnSignupPoint(Long userId);

    // 3-2. 리뷰 작성 적립 (일반/사진)
    EarnPointResponseDto earnReviewPoint(EarnReviewPointRequestDto dto);

    // 3-3. 도서 결제 적립 (적립률)
    EarnPointResponseDto earnOrderPoint(EarnOrderPointRequestDto dto);

    // 3-4. 등급 적립 (내부용, 필요하면 리턴 타입도 DTO로 바꿔도 됨)
    void earnGradePoint(Long userId, int pureAmount, double gradeRewardRate);

    // 4. 포인트 사용
    EarnPointResponseDto usePoint(UsePointRequestDto dto);

    // 5. 포인트 반환 (결제취소/반품)
    EarnPointResponseDto refundPoint(RefundPointRequestDto dto);

    // 6. 포인트 만료 처리
    void expirePoints(Long userId);

    // 7. 관리자 수동 포인트 지급/차감
    EarnPointResponseDto adjustPointByAdmin(PointHistoryAdminAdjustRequestDto requestDto);
}



