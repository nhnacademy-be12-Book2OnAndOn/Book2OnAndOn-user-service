package com.example.book2onandonuserservice.global.client;

import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//분기별 순수 주문금액을 조회하기 위한 feign 클라이언트 인터페이스
@FeignClient(name = "")
public interface OrderServiceClient {
    //특정기간 동안의 순수 주문금액 조회 (API는 주문쪽 API 정해지면 수정)
    @GetMapping("/api/???")
    Long getNetOrderAmount(
            @PathVariable("userId") Long userId,
            @RequestParam("from") LocalDate fromDate,
            @RequestParam("to") LocalDate toDate
    );
}
