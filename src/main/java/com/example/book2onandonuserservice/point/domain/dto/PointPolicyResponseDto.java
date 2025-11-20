package com.example.book2onandonuserservice.point.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointPolicyResponseDto {

    private Long pointPolicyId;
    private String pointPolicyName;
    private Double pointAddRate;
    private Integer pointAddPoint;
    private String pointPolicyReason;

}
