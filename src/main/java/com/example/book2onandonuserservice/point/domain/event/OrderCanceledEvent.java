package com.example.book2onandonuserservice.point.domain.event;

import java.time.LocalDateTime;

public record OrderCanceledEvent(
        Long userId,
        Long orderId,
        Integer usedPoint,
        LocalDateTime occurredAt
) {
}
