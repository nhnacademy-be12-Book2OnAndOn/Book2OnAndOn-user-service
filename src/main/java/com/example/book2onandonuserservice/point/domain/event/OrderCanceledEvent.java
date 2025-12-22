package com.example.book2onandonuserservice.point.domain.event;

import java.time.LocalDateTime;

public record OrderCanceledEvent(
        String eventId,
        Long userId,
        Long orderId,
        Integer usedPoint,
        LocalDateTime occurredAt
) {
}
