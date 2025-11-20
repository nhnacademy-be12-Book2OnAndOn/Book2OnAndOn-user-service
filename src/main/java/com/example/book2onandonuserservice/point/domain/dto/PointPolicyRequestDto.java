package com.example.book2onandonuserservice.point.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PointPolicyRequestDto {

    @Size(max = 50)
    @NotBlank(message = "포인트 정책 이름은 필수입니다.")
    private String pointPolicyName;

    // 비율 기반 적립일 때 사용 (예: 0.01 = 1%)
    @PositiveOrZero(message = "비율은 양수 혹은 0이여야 합니다.")
    private Double pointAddRate;

    // 고정 포인트 적립일 때 사용 (예: 회원가입 5000P, 리뷰 200P 등)
    @PositiveOrZero(message = "포인트는 양수 혹은 0이여야 합니다.")
    private Integer pointAddPoint;

    @Size(max = 50)
    @NotBlank(message = "포인트 정책 사유는 필수입니다.")
    private String pointPolicyReason;
}
