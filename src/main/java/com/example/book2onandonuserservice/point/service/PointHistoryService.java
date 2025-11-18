package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.PointHistoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointHistoryService {

    // 1. 포인트 내역 조회
    Page<PointHistoryResponseDto> getMyPointHistory(Long userId, Pageable pageable);

    // 2. 현재 포인트 조회
    PointHistoryResponseDto getMyCurrentPoint(Long userId);

    // 3-1. 회원가입 적립
    void earnSignupPoint(Long userId);

    // 3-2. 리뷰 작성 적립
    void earnReviewPoint(Long userId, Long reviewId);

    // 3-3. 주문 적립 (적립률)
    void earnOrderPoint(Long userId, Long orderItemId, int orderAmount);

    // 4. 포인트 사용
    void usePoint(Long userId, Long orderItemId, int useAmount);

    // 5. 포인트 반환 (주문 취소/반품)
    void refundPoint(Long userId, Long orderItemId);

    // 6. 포인트 만료 처리
    void expirePoints(Long userId);
}


