package com.example.book2onandonuserservice.point.support.pointhistory;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.InactivePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointCalculationHelper {

    private final PointPolicyRepository pointPolicyRepository;

    // PointReason 기반 정책 조회 후 고정 포인트 계산
    public int calculateByReason(PointReason reason) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(reason.name())
                .orElseThrow(() -> new PointPolicyNotFoundException(reason.name()));
        return calculate(policy);
    }

    // policyName 기반 계산 (REVIEW_TEXT, REVIEW_PHOTO 등)
    public int calculateByPolicyName(String policyName) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(policyName)
                .orElseThrow(() -> new PointPolicyNotFoundException(policyName));
        return calculate(policy);
    }

    private int calculate(PointPolicy policy) {

        // 1) 비활성화 처리
        if (!Boolean.TRUE.equals(policy.getPolicyIsActive())) {
            // 택 1 (예외를 던질지, 0을 반환할지)
            throw new InactivePointPolicyException(policy.getPolicyName());
        }
        // 2) 고정 포인트 검증
        Integer point = policy.getPolicyAddPoint();
        if (point == null || point < 0) {
            throw new InvalidPointPolicyException("고정 포인트는 0 이상으로 설정되어야 합니다. id=" + policy.getPolicyId());
        }

        // 3) 정상 리턴
        return point;
    }
}