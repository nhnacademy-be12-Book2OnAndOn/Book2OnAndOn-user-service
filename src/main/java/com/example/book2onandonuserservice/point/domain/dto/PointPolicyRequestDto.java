package com.example.book2onandonuserservice.point.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointPolicyRequestDto {

    @Size(max = 50)
    @NotBlank
    private String pointPolicyName;

    // 비율 기반 적립일 때 사용 (예: 0.01 = 1%)
    @PositiveOrZero
    private Double pointAddRate;

    // 고정 포인트 적립일 때 사용 (예: 회원가입 5000P, 리뷰 200P 등)
    @PositiveOrZero
    private Integer pointAddPoint;
}
