package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
import java.util.List;

public interface PointPolicyService {

    PointPolicyResponseDto createPolicy(PointPolicyRequestDto requestDto);

    List<PointPolicyResponseDto> getAllPolicies();

    PointPolicyResponseDto getPolicyByName(String name);

    PointPolicyResponseDto updatePolicy(Long policyId, PointPolicyRequestDto requestDto);
}
