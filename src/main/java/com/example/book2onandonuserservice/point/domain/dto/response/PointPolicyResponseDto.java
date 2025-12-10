package com.example.book2onandonuserservice.point.domain.dto.response;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointPolicyResponseDto {

    private Integer pointPolicyId;
    private String pointPolicyName;
    //    private Double pointAddRate;
    private Integer pointAddPoint;
    private Boolean pointIsActive;

    // 포인트 이력(PointHistory)의 단건을 DTO로 바꾸는 메서드
    public static PointPolicyResponseDto toDto(PointPolicy pointPolicy) {
        return PointPolicyResponseDto.builder()
                .pointPolicyId(pointPolicy.getPolicyId())
                .pointPolicyName(pointPolicy.getPolicyName())
//                .pointAddRate(pointPolicy.getPolicyAddRate())
                .pointAddPoint(pointPolicy.getPolicyAddPoint())
                .pointIsActive(pointPolicy.getPolicyIsActive())
                .build();
    }

}
