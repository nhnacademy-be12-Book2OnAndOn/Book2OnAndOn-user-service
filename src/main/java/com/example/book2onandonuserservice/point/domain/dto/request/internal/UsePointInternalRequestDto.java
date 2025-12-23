package com.example.book2onandonuserservice.point.domain.dto.request.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsePointInternalRequestDto {
    private Long orderId;
    private Integer useAmount;
    private Integer allowedMaxUseAmount;
}
