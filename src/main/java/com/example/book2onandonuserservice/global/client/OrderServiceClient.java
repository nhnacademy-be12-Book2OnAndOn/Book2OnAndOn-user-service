package com.example.book2onandonuserservice.global.client;

import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//분기별 순수 주문금액을 조회하기 위한 feign 클라이언트 인터페이스
@FeignClient(name = "order-payment-service")
public interface OrderServiceClient {
    //특정기간 동안의 순수 주문금액 조회
    @GetMapping("/orders/users/{userId}/net-amount")
    Long getNetOrderAmount(
            @PathVariable("userId") Long userId,
            @RequestParam("from") LocalDate fromDate,
            @RequestParam("to") LocalDate toDate
    );

    // 개별 주문 기준 순수금액 조회
    @GetMapping("/{orderId}/pure-amount")
    Integer getOrderPureAmount(
            @PathVariable("orderId") Long orderId
    );

    // 개별 주문 기준 사용 포인트 조회
    @GetMapping("/{orderId}/used-point")
    Integer getOrderUsedPoint(
            @PathVariable("orderId") Long orderId
    );

    // 반품 기준 환불(반품) 금액 조회
    @GetMapping("/return/{refundId}/amount")
    Integer getReturnAmount(
            @PathVariable("refundId") Long refundId
    );
}
