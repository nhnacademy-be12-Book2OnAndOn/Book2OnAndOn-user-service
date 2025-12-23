package com.example.book2onandonuserservice.point.domain.dto.request.internal;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundPointInternalRequestDto {
    private Long orderId;
    private Long refundId;
    private Integer usedPoint;      // 포인트 결제분 복구, nullable 가능
    private Integer refundAmount;   // 현금 결제분을 포인트로 적립, nullable 가능

    @NotNull
    private Long externalTxId; // refundId 권장 (멱등키)
}
