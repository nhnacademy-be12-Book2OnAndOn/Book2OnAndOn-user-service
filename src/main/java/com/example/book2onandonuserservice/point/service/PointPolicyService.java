package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import java.util.List;

public interface PointPolicyService {

    // 1. 포인트 정책 전체 조회
    List<PointPolicyResponseDto> getAllPolicies();

    // 2. 포인트 정책 단건 조회
    PointPolicyResponseDto getPolicyByName(String name);

    // 3. 포인트 정책 수정
    PointPolicyResponseDto updatePolicyPoint(Integer policyId, PointPolicyUpdateRequestDto dto);

    // 4. 포인트 정책 활성/비활성
    PointPolicyResponseDto updatePolicyActive(Integer policyId, PointPolicyActiveUpdateRequestDto dto);

}
