package com.example.book2onandonuserservice.global.service;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final WebClient couponWebClient;

    @Async
    public CompletableFuture<Void> issueWelcomeCoupon(Long userId) {
        return couponWebClient.post()
                .uri("/coupons/welcome")
                .header("X-User-Id", String.valueOf(userId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> {
                    // 실패 로깅
                    log.error("WelcomeCoupon 발급 실패: userId={}, error={}", userId, error.getMessage());
                })
                .then()
                .toFuture();
    }
}