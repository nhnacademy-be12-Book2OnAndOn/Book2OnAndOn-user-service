package com.example.book2onandonuserservice.global.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final WebClient couponWebClient;

    @Async
    public CompletableFuture<Void> issueWelcomeCoupon(Long userId) {
        Map<String, Long> requestBody = Map.of("userId", userId);

        return couponWebClient.post()
                .uri("/coupons/welcome")
                .body(Mono.just(requestBody), Map.class)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> {
                    log.error("WelcomeCoupon 발급 실패: userId={}, error={}", userId, error.getMessage());
                })
                .then()
                .toFuture();
    }
}