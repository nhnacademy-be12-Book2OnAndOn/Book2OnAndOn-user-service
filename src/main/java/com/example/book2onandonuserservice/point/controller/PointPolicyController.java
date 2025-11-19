package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.PointPolicyRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.PointPolicyResponseDto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    // 1. 새 포인트 정책 생성 (201)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PointPolicyResponseDto setNewPointPolicy(@Valid @RequestBody PointPolicyRequestDto pointPolicyRequestDto) {
        return pointPolicyService.createPolicy(pointPolicyRequestDto);
    }

    // 2. 전체 포인트 정책 목록 조회 (200)
    @GetMapping
    public List<PointPolicyResponseDto> checkAllPointPolicies() {
        return pointPolicyService.getAllPolicies();
    }

    // 3. 포인트 정책 단건 조회 (200)
    @GetMapping("/{policyName}")
    public PointPolicyResponseDto checkOnePointPolicy(@RequestParam String name) {
        return pointPolicyService.getPolicyByName(name);
    }

    // 4. 특정 포인트 정책 수정 (200)
    @PutMapping("/{policyId}")
    public PointPolicyResponseDto updatePointPolicy(@PathVariable Long policyId,
                                                    @Valid @RequestBody PointPolicyRequestDto pointPolicyRequestDto) {
        return pointPolicyService.updatePolicy(policyId, pointPolicyRequestDto);
    }
}

