package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/points")
@RequiredArgsConstructor
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;

    // 1. 내 포인트 전체 이력 조회
    @GetMapping("/history")
    public Page<PointHistoryResponseDto> getMyPointHistory(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        return pointHistoryService.getMyPointHistory(userId, pageable);
    }

    // 2. 현재 포인트 조회
    @GetMapping("/current")
    public PointHistoryResponseDto getMyCurrentPoint(
            @RequestParam Long userId
    ) {
        return pointHistoryService.getMyCurrentPoint(userId);
    }

    // 3. 포인트 적립
    // 3-1. 회원가입 적립
    @PostMapping("/earn/signup")
    public ResponseEntity<Void> earnSignupPoint(@RequestParam Long userId) {
        pointHistoryService.earnSignupPoint(userId);
        return ResponseEntity.ok().build();
    }

    // 3-2. 리뷰 적립
    @PostMapping("/earn/review")
    public ResponseEntity<Void> earnReviewPoint(@RequestParam Long userId, @RequestParam Long reviewId) {
        pointHistoryService.earnReviewPoint(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    // 3-3. 주문 적립
    @PostMapping("/earn/order")
    public ResponseEntity<Void> earnOrderPoint(@RequestParam Long userId, @RequestParam Long orderItemId, @RequestParam int orderAmount) {
        pointHistoryService.earnOrderPoint(userId, orderItemId, orderAmount);
        return ResponseEntity.ok().build();
    }

    // 4. 포인트 사용
    // POST /users/me/points/use?userId=1&orderItemId=100&useAmount=3000
    @PostMapping("/use")
    public ResponseEntity<Void> usePoint(@RequestParam Long userId, @RequestParam Long orderItemId, @RequestParam int useAmount) {
        pointHistoryService.usePoint(userId, orderItemId, useAmount);
        return ResponseEntity.ok().build();
    }

    // 5. 포인트 반환 (주문 취소/반품)
    // POST /users/me/points/refund?userId=1&orderItemId=100
    @PostMapping("/refund")
    public ResponseEntity<Void> refundPoint(@RequestParam Long userId, @RequestParam Long orderItemId) {
        pointHistoryService.refundPoint(userId, orderItemId);
        return ResponseEntity.ok().build();
    }

    // 6. 포인트 만료 처리
    //    POST /users/me/points/expire?userId=1
    @PostMapping("/expire")
    public ResponseEntity<Void> expirePoints(@RequestParam Long userId) {
        pointHistoryService.expirePoints(userId);
        return ResponseEntity.ok().build();
    }
}
