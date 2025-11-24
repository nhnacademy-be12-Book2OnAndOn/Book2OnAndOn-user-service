package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.support.pointPolicy.PointPolicyValidator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;
    private final PointPolicyValidator pointPolicyValidator;

    // 1. 정책 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<PointPolicyResponseDto> getAllPolicies() {
        return pointPolicyRepository.findAll().stream()
                .map(entity -> PointPolicyResponseDto.toDto(entity))
                .collect(Collectors.toList());
    }

    // 2. 정책 단건 조회
    @Override
    @Transactional(readOnly = true)
    public PointPolicyResponseDto getPolicyByName(String name) {
        return pointPolicyRepository.findByPolicyName(name)
                .map(entity -> PointPolicyResponseDto.toDto(entity))
                .orElseThrow(() -> new PointPolicyNotFoundException(name));
    }

    // 3. 정책 비율/포인트 수정
    @Override
    public PointPolicyResponseDto updatePolicyRateAndPoint(Integer policyId, PointPolicyUpdateRequestDto dto) {
        PointPolicy policy = pointPolicyRepository.findById(policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException(policyId));

        pointPolicyValidator.validateRateAndPoint(dto.getPointAddRate(), dto.getPointAddPoint());

        if (dto.getPointAddRate() != null) {
            policy.setPolicyAddRate(dto.getPointAddRate());
            policy.setPolicyAddPoint(null);
        }
        if (dto.getPointAddPoint() != null) {
            policy.setPolicyAddRate(null);
            policy.setPolicyAddPoint(dto.getPointAddPoint());
        }
        return PointPolicyResponseDto.toDto(policy);
    }

    // 4. 정책 활성/비활성
    @Override
    public PointPolicyResponseDto updatePolicyActive(Integer policyId, PointPolicyActiveUpdateRequestDto dto) {
        PointPolicy policy = pointPolicyRepository.findById(policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException(policyId));

        policy.setPolicyIsActive(dto.getIsActive());
        // 필요하면, 최소 1개 기본 정책은 항상 활성이어야 한다. 같은 추가 검증 넣기
        return PointPolicyResponseDto.toDto(policy);
    }

}
