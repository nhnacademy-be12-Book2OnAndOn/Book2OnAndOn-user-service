package com.example.book2onandonuserservice.point.service;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.support.pointPolicy.PointPolicyMapper;
import com.example.book2onandonuserservice.point.support.pointPolicy.PointPolicyValidator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;
    private final PointPolicyValidator pointPolicyValidator;
    private final PointPolicyMapper pointPolicyMapper;

    // 1. 새 정책 생성
    @Override
    @Transactional
    public PointPolicyResponseDto createPolicy(PointPolicyRequestDto dto) {
        pointPolicyValidator.validateReason(dto.getPointPolicyReason());
        pointPolicyValidator.validateRateAndPoint(dto.getPointAddRate(), dto.getPointAddPoint());
        pointPolicyValidator.checkDuplicate(dto.getPointPolicyName());

        PointPolicy entity = pointPolicyMapper.toEntity(dto);
        PointPolicy saved = pointPolicyRepository.save(entity);

        return pointPolicyMapper.toDto(saved);
    }

    // 2. 정책 전체 조회
    @Override
    public List<PointPolicyResponseDto> getAllPolicies() {
        return pointPolicyRepository.findAll().stream()
                .map(policy -> pointPolicyMapper.toDto(policy))
                .collect(Collectors.toList());
    }

    // 3. 정책 단건 조회
    @Override
    public PointPolicyResponseDto getPolicyByName(String name) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(name)
                .orElseThrow(() -> new PointPolicyNotFoundException(name));
        return pointPolicyMapper.toDto(policy);
    }

    // 4. 정책 수정(비율/포인트 수정만)
    @Override
    @Transactional
    public PointPolicyResponseDto updatePolicy(Long id, PointPolicyUpdateRequestDto dto) {
        PointPolicy policy = pointPolicyRepository.findById(id)
                .orElseThrow(() -> new PointPolicyNotFoundException(id));

        pointPolicyValidator.validateRateAndPoint(dto.getPointAddRate(), dto.getPointAddPoint());

        // 비율 수정
        if (dto.getPointAddRate() != null) {
            policy.setAddRate(dto.getPointAddRate());
        }
        // 고정포인트 수정
        if (dto.getPointAddPoint() != null) {
            policy.setAddPoint(dto.getPointAddPoint());
        }

        return pointPolicyMapper.toDto(policy);
    }
}
