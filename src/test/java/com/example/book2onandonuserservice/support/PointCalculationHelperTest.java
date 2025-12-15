package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.exception.InactivePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.support.pointhistory.PointCalculationHelper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointCalculationHelperTest {

    @Mock
    PointPolicyRepository repository;

    @InjectMocks
    PointCalculationHelper helper;

    @Test
    @DisplayName("PointReason 기반 정책 포인트 계산 성공")
    void calculateByReason_success() {
        PointPolicy policy = new PointPolicy(1, "SIGNUP", 50, true);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        int result = helper.calculateByReason(PointReason.SIGNUP);

        assertEquals(50, result);
    }

    @Test
    @DisplayName("정책 이름 기반 정책 포인트 계산 성공")
    void calculateByPolicyName_success() {
        PointPolicy policy = new PointPolicy(1, "SIGNUP", 50, true);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        int result = helper.calculateByPolicyName("SIGNUP");

        assertEquals(50, result);
    }

    @Test
    @DisplayName("PointReason 기반 조회 시 정책 부재 -> PointPolicyNotFoundException 발생")
    void calculateByReason_notFound() {
        when(repository.findByPolicyName("REVIEW"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> helper.calculateByReason(PointReason.REVIEW));
    }

    @Test
    @DisplayName("정책 이름 기반 조회 시 정책 부재 -> PointPolicyNotFoundException 발생")
    void calculateByPolicyName_notFound() {
        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> helper.calculateByPolicyName("SIGNUP"));
    }

    @Test
    @DisplayName("비활성화 정책 조회 시 -> InactivePointPolicyException 발생")
    void calculate_inactivePolicy_throws() {
        PointPolicy policy = new PointPolicy(1, "SIGNUP", 50, false);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InactivePointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));
    }

    @Test
    @DisplayName("정책 포인트 null 값 -> InvalidPointPolicyException 발생")
    void calculate_nullPoint_throws() {
        // point가 Integer 라면 null로 만들 수 있음
        PointPolicy policy = new PointPolicy(1, "SIGNUP", null, true);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InvalidPointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));
    }

    @Test
    @DisplayName("정책 포인트 음수 값 -> InvalidPointPolicyException 발생")
    void calculate_negativePoint_throws() {
        PointPolicy policy = new PointPolicy(1, "SIGNUP", -10, true);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InvalidPointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));
    }
}
