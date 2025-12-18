package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.EarnOrderPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.EarnReviewPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.RefundPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.UsePointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users/{userId}/points") // 라우팅/인증으로 막아야 하는 엔드포인트(게이트웨이에서 internal prefix 차단, mTLS/서버간 토큰 등)
@RequiredArgsConstructor
public class PointInternalController {

    private final PointHistoryService pointHistoryService;

    // 1. 회원가입 적립
//    @PostMapping("/earn/signup")
//    public ResponseEntity<EarnPointResponseDto> earnSignupPoint(@PathVariable Long userId) {
//        EarnPointResponseDto earnPoint = pointHistoryService.earnSignupPoint(userId);
//        return ResponseEntity.ok(earnPoint);
//    }

    // 2. 리뷰 작성 적립 (일반/사진)
    @PostMapping("/earn/review")
    public ResponseEntity<EarnPointResponseDto> earnReviewPoint(
            @PathVariable Long userId,
            @RequestBody EarnReviewPointInternalRequestDto requestDto
    ) {
        validateUserIdMatch(userId, requestDto.getUserId());

        EarnReviewPointRequestDto dto = new EarnReviewPointRequestDto();
        dto.setUserId(userId);
        dto.setReviewId(dto.getReviewId());
        dto.setHasImage(dto.isHasImage());

        return ResponseEntity.ok(pointHistoryService.earnReviewPoint(dto));
    }

    // 3. 도서 결제 적립 (적립률)
    public ResponseEntity<EarnPointResponseDto> earnOrderPoint(
            @PathVariable Long userId,
            @RequestBody EarnOrderPointInternalRequestDto requestDto
    ) {
        validateUserIdMatch(userId, requestDto.getUserId());

        EarnOrderPointRequestDto dto = new EarnOrderPointRequestDto();
        dto.setUserId(userId);
        dto.setOrderId(requestDto.getOrderId());
        dto.setPureAmount(requestDto.getPureAmount());
        dto.setPointAddRate(requestDto.getPointAddRate());

        return ResponseEntity.ok(pointHistoryService.earnOrderPoint(dto));
    }

    // 4. 포인트 사용
    @PostMapping("/use")
    public ResponseEntity<EarnPointResponseDto> usePoint(
            @PathVariable Long userId,
            @RequestBody UsePointInternalRequestDto requestDto
    ) {
        validateUserIdMatch(userId, requestDto.getUserId());

        UsePointRequestDto dto = new UsePointRequestDto();
        dto.setUserId(userId);
        dto.setOrderId(dto.getOrderId());
        dto.setUseAmount(dto.getUseAmount());
        dto.setAllowedMaxUseAmount(dto.getAllowedMaxUseAmount());

        EarnPointResponseDto earnPoint = pointHistoryService.usePoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 5. 포인트 반환 (결제취소/반품)
    @PostMapping("/refund")
    public ResponseEntity<EarnPointResponseDto> refundPoint(
            @PathVariable Long userId,
            @RequestBody RefundPointInternalRequestDto requestDto
    ) {
        validateUserIdMatch(userId, requestDto.getUserId());

        RefundPointRequestDto dto = new RefundPointRequestDto();
        dto.setUserId(userId);
        dto.setOrderId(dto.getOrderId());
        dto.setReturnId(dto.getReturnId());
        dto.setUsedPoint(dto.getUsedPoint());
        dto.setReturnAmount(dto.getReturnAmount());

        return ResponseEntity.ok(pointHistoryService.refundPoint(dto));
    }

    private void validateUserIdMatch(Long pathUserId, Long bodyUserId) {
        if (bodyUserId == null) {
            throw new IllegalArgumentException("body userId는 필수입니다.");
        }
        if (!pathUserId.equals(bodyUserId)) {
            throw new IllegalArgumentException("path userId와 body userId가 일치하지 않습니다.");
        }
    }

}
