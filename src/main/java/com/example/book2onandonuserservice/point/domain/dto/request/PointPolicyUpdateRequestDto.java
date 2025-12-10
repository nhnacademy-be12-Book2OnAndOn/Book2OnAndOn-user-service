package com.example.book2onandonuserservice.point.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointPolicyUpdateRequestDto {

    // 비율 기반 적립 (null 허용)
//    @PositiveOrZero(message = "비율은 0 이상이어야 합니다.")
//    private Double pointAddRate;

    // 고정 포인트 적립 (null 허용)
//    @PositiveOrZero(message = "포인트는 0 이상이어야 합니다.")
    @NotNull(message = "포인트는 0 이상이어야 합니다.")
    private Integer pointAddPoint;

}
