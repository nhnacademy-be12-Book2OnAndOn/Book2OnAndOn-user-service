package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;

    // 1. 새 포인트 정책 생성
    @Transactional
    public PointPolicyResponseDto createPolicy(PointPolicyRequestDto dto) {
        PointPolicy policy = PointPolicy.builder()
                .policyName(dto.getPointPolicyName())
                .addRate(dto.getPointAddRate())
                .addPoint(dto.getPointAddPoint())
                .build();
        PointPolicy saved = pointPolicyRepository.save(policy);
        return convertToResponse(saved);
    }

    // 2. 전체 정책 목록 조회
    public List<PointPolicyResponseDto> getAllPolicies() {
        return pointPolicyRepository.findAll().stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 3. 정책 단건 조회
    public PointPolicyResponseDto getPolicyByName(String name) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(name)
                .orElseThrow(() -> new IllegalArgumentException("정책 없음: " + name));
        return convertToResponse(policy);
    }

    // 4. 포인트 정책 수정
    @Transactional
    public PointPolicyResponseDto updatePolicy(Long id, PointPolicyRequestDto pointPolicyRequestDto) {
        PointPolicy policy = pointPolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("정책 없음: " + id));
        if (pointPolicyRequestDto.getPointPolicyName() != null) {
            policy.setPolicyName(pointPolicyRequestDto.getPointPolicyName());
        }
        policy.setAddRate(pointPolicyRequestDto.getPointAddRate());
        policy.setAddPoint(pointPolicyRequestDto.getPointAddPoint());

        return convertToResponse(policy);
    }

    // (보조). Entity -> DTO로 변환
    private PointPolicyResponseDto convertToResponse(PointPolicy pointPolicy) {
        return PointPolicyResponseDto.builder()
                .pointPolicyId(pointPolicy.getPolicyId())
                .pointPolicyName(pointPolicy.getPolicyName())
                .pointAddRate(pointPolicy.getAddRate())
                .pointAddPoint(pointPolicy.getAddPoint())
                .build();
    }
}

