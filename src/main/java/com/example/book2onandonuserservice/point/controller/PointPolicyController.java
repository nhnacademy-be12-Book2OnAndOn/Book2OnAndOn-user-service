package com.example.book2onandonuserservice.point.controller;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.service.PointPolicyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;
    private static final String USER_ID_HEADER = "X-User-Id";

    // 1. (관리자) 정책 전체 조회
    // GET /admin/point-policies
    @GetMapping
    public ResponseEntity<List<PointPolicyResponseDto>> getAllPolicies(
            @RequestHeader(USER_ID_HEADER) Long userId // 당장은 안 쓰지만, “어떤 관리자가 이 작업을 했는지” 추후 로깅/감사에 쓰기 위해 작성...?
    ) {
        List<PointPolicyResponseDto> pointPolicy = pointPolicyService.getAllPolicies();
        return ResponseEntity.ok(pointPolicy);
    }

    // 2. (관리자) 정책 단건 조회
    // GET /admin/point-policies/SIGNUP
    @GetMapping("/{policyName}")
    public ResponseEntity<PointPolicyResponseDto> getPolicy(
            @PathVariable String policyName,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        PointPolicyResponseDto pointPolicy = pointPolicyService.getPolicyByName(policyName);
        return ResponseEntity.ok(pointPolicy);
    }

    // 3. (관리자) 정책 비율/고정포인트 수정
    // PUT /admin/point-policies/1
    @PutMapping("/{policyId}")
    public ResponseEntity<PointPolicyResponseDto> updatePolicy(
            @PathVariable Integer policyId,
            @Valid @RequestBody PointPolicyUpdateRequestDto dto,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        PointPolicyResponseDto pointPolicy = pointPolicyService.updatePolicyPoint(policyId, dto);
        return ResponseEntity.ok(pointPolicy);
    }

    // 4. (관리자) 정책 활성/비활성
    // PATCH /admin/point-policies/1/active
    @PatchMapping("/{policyId}/active")
    public ResponseEntity<PointPolicyResponseDto> updatePolicyActive(
            @PathVariable Integer policyId,
            @Valid @RequestBody PointPolicyActiveUpdateRequestDto dto,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        PointPolicyResponseDto pointPolicy = pointPolicyService.updatePolicyActive(policyId, dto);
        return ResponseEntity.ok(pointPolicy);
    }

}