package com.example.book2onandonuserservice.point.support.pointpolicy;

import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import org.springframework.stereotype.Component;

@Component
public class PointPolicyValidator {

    // (보조). 정책 비율/포인트 검증
    public void validatePoint(Integer point) {

        if (point == null) {
            throw new InvalidPointPolicyException("고정 포인트는 반드시 설정해야 합니다.");
        }
        if (point < 0) {
            throw new InvalidPointPolicyException("고정 포인트는 0 이상이어야 합니다.");
        }
    }

}
