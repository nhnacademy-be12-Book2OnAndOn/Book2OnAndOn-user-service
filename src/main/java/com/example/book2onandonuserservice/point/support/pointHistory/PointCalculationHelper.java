package com.example.book2onandonuserservice.point.support.pointHistory;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.InactivePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.support.pointPolicy.PointPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointCalculationHelper {

    private final PointPolicyRepository pointPolicyRepository;
    private final PointPolicyValidator pointPolicyValidator;

    // PointReason(SIGNUP, REVIEW, ORDER 등) 이름 그대로 정책을 찾는 경우
    public int calculateByReason(PointReason reason, Integer baseAmount) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(reason.name())
                .orElseThrow(() -> new PointPolicyNotFoundException(reason.name()));
        return calculate(policy, baseAmount);
    }

    // 별도 정책명(REVIEW_PHOTO 등)을 직접 넘기는 경우
    public int calculateByPolicyName(String policyName, Integer baseAmount) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName(policyName)
                .orElseThrow(() -> new PointPolicyNotFoundException(policyName));
        return calculate(policy, baseAmount);
    }

    private int calculate(PointPolicy policy, Integer baseAmount) {
        if (!policy.getPolicyIsActive()) {
            throw new InactivePointPolicyException(policy.getPolicyName());
        }

        Double rate = policy.getPolicyAddRate();
        Integer point = policy.getPolicyAddPoint();

        pointPolicyValidator.validateRateAndPoint(rate, point);

        // 고정 포인트 우선
        if (point != null) {
            return point;
        }
        // 비율 적립
        if (rate != null) {
            if (baseAmount == null || baseAmount <= 0) {
                throw new IllegalArgumentException("비율 적립 정책에는 유효한 baseAmount(주문 금액)가 필요합니다.");
            }
            return (int) Math.round(baseAmount * rate);
        }

        throw new InvalidPointPolicyException("포인트 정책 설정이 잘못되었습니다. id=" + policy.getPolicyId());
    }
}