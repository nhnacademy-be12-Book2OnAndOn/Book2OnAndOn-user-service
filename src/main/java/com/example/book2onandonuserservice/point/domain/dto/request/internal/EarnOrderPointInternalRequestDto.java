package com.example.book2onandonuserservice.point.domain.dto.request.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarnOrderPointInternalRequestDto {
    private Long orderId;
    private Integer pureAmount;
    private Double pointAddRate;
}
