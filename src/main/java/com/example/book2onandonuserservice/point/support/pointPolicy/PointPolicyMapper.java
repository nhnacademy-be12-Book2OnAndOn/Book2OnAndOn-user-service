package com.example.book2onandonuserservice.point.support.pointPolicy;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import org.springframework.stereotype.Component;

@Component
public class PointPolicyMapper {

    // (보조). 데이터가 서비스로 들어올 때 : DTO → Entity
    public PointPolicy toEntity(PointPolicyRequestDto dto) {
        return PointPolicy.builder()
                .policyName(dto.getPointPolicyName())
                .addRate(dto.getPointAddRate())
                .addPoint(dto.getPointAddPoint())
                .policyReason(dto.getPointPolicyReason())
                .build();
    }

    // (보조). 데이터가 서비스로 들어올 때 : DTO → Entity
    public PointPolicyResponseDto toDto(PointPolicy policy) {
        return PointPolicyResponseDto.builder()
                .pointPolicyId(policy.getPolicyId())
                .pointPolicyName(policy.getPolicyName())
                .pointAddRate(policy.getAddRate())
                .pointAddPoint(policy.getAddPoint())
                .pointPolicyReason(policy.getPolicyReason())
                .build();
    }
}
