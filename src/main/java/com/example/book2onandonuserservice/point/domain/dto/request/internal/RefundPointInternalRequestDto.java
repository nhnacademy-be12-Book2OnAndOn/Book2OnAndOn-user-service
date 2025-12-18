package com.example.book2onandonuserservice.point.domain.dto.request.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundPointInternalRequestDto {
    private Long userId;
    private Long orderId;
    private Long returnId;
    private Integer usedPoint;      // nullable 가능
    private Integer returnAmount;   // nullable 가능
}
