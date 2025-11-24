package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import java.util.List;

public interface PointPolicyService {

    List<PointPolicyResponseDto> getAllPolicies();

    PointPolicyResponseDto getPolicyByName(String name);

    PointPolicyResponseDto updatePolicyRateAndPoint(Integer policyId, PointPolicyUpdateRequestDto dto);

    PointPolicyResponseDto updatePolicyActive(Integer policyId, PointPolicyActiveUpdateRequestDto dto);

}
