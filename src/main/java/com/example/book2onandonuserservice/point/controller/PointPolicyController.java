package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.service.PointPolicyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    // 1. 정책 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PointPolicyResponseDto createPolicy(
            @Valid @RequestBody PointPolicyRequestDto dto
    ) {
        return pointPolicyService.createPolicy(dto);
    }

    // 2. 전체 정책 조회
    @GetMapping
    public List<PointPolicyResponseDto> getAllPolicies() {
        return pointPolicyService.getAllPolicies();
    }

    // 3. 정책 단건 조회
    @GetMapping("/{policyName}")
    public PointPolicyResponseDto getPolicy(
            @PathVariable String policyName
    ) {
        return pointPolicyService.getPolicyByName(policyName);
    }

    // 4. 특정 정책 수정
    @PutMapping("/{policyId}")
    public PointPolicyResponseDto updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody PointPolicyUpdateRequestDto dto
    ) {
        return pointPolicyService.updatePolicy(policyId, dto);
    }

    
}
