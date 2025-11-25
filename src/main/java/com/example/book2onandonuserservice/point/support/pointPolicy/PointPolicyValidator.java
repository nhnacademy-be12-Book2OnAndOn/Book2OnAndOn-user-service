package com.example.book2onandonuserservice.point.support.pointPolicy;

import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import org.springframework.stereotype.Component;

@Component
public class PointPolicyValidator {

    // (보조). 정책 비율/포인트 검증
    public static void validateRateAndPoint(Double rate, Integer point) {
        if (rate == null && point == null) {
            throw new InvalidPointPolicyException("비율 또는 고정포인트 중 하나는 반드시 설정해야 합니다.");
        }
        if (rate != null && point != null) {
            throw new InvalidPointPolicyException("비율과 고정포인트는 동시에 설정할 수 없습니다.");
        }
        if (rate != null && (rate < 0 || rate > 1)) {
            throw new InvalidPointPolicyException("적립률은 0~1 사이여야 합니다.");
        }
        if (point != null && point < 0) {
            throw new InvalidPointPolicyException("고정 포인트는 0 이상이어야 합니다.");
        }
    }

}
