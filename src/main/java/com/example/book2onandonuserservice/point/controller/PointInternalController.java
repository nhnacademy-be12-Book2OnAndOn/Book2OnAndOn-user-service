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

    // 2. 리뷰 작성 적립 (일반/사진)
    @PostMapping("/earn/review")
    public ResponseEntity<EarnPointResponseDto> earnReviewPoint(
            @PathVariable Long userId,
            @RequestBody EarnReviewPointInternalRequestDto requestDto
    ) {
        EarnReviewPointRequestDto dto = new EarnReviewPointRequestDto();
        dto.setUserId(userId);
        dto.setReviewId(requestDto.getReviewId());
        dto.setHasImage(requestDto.isHasImage());

        return ResponseEntity.ok(pointHistoryService.earnReviewPoint(dto));
    }

    // 3. 도서 결제 적립 (적립률)
    @PostMapping("/earn/order")
    public ResponseEntity<EarnPointResponseDto> earnOrderPoint(
            @PathVariable Long userId,
            @RequestBody EarnOrderPointInternalRequestDto requestDto
    ) {
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
        UsePointRequestDto dto = new UsePointRequestDto();
        dto.setUserId(userId);
        dto.setOrderId(requestDto.getOrderId());
        dto.setUseAmount(requestDto.getUseAmount());

        EarnPointResponseDto earnPoint = pointHistoryService.usePoint(dto);
        return ResponseEntity.ok(earnPoint);
    }

    // 5. 포인트 반환 (반품만) -> order-service에서 “취소/반품을 refundPoint 하나로 몰아서 호출”하면 멱등/정합성이 깨질 수 있다.
    // -> 결제 롤백 포인트 반환(cancel)이 따로 있음.
    @PostMapping("/refund")
    public ResponseEntity<EarnPointResponseDto> refundPoint(
            @PathVariable Long userId,
            @RequestBody RefundPointInternalRequestDto requestDto
    ) {
        RefundPointRequestDto dto = new RefundPointRequestDto();
        dto.setUserId(userId);
        dto.setOrderId(requestDto.getOrderId());
        dto.setRefundId(requestDto.getRefundId());
        dto.setUsedPoint(requestDto.getUsedPoint());
        dto.setRefundAmount(requestDto.getRefundAmount());

        return ResponseEntity.ok(pointHistoryService.refundPoint(dto));
    }
}
