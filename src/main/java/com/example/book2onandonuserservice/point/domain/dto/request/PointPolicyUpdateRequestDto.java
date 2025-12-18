package com.example.book2onandonuserservice.point.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointPolicyUpdateRequestDto {
    @NotNull(message = "포인트는 필수입니다.")
    @Min(value = 0, message = "포인트는 0 이상이어야 합니다.")
    private Integer pointAddPoint;
}
