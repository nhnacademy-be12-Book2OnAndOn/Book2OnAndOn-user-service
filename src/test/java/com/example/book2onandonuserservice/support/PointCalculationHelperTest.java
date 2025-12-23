package com.example.book2onandonuserservice.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPolicyIsActive()).thenReturn(true);
        when(policy.getPolicyAddPoint()).thenReturn(50);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        int result = helper.calculateByReason(PointReason.SIGNUP);

        assertEquals(50, result);
        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("정책 이름 기반 정책 포인트 계산 성공")
    void calculateByPolicyName_success() {
        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPolicyIsActive()).thenReturn(true);
        when(policy.getPolicyAddPoint()).thenReturn(50);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        int result = helper.calculateByPolicyName("SIGNUP");

        assertEquals(50, result);
        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("PointReason 기반 조회 시 정책 부재 -> PointPolicyNotFoundException 발생")
    void calculateByReason_notFound() {
        // 존재하는 enum 상수로 테스트 (예: SIGNUP)
        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));

        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("정책 이름 기반 조회 시 정책 부재 -> PointPolicyNotFoundException 발생")
    void calculateByPolicyName_notFound() {
        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> helper.calculateByPolicyName("SIGNUP"));

        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("비활성화 정책 조회 시 -> InactivePointPolicyException 발생")
    void calculate_inactivePolicy_throws() {
        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPolicyIsActive()).thenReturn(false);
        when(policy.getPolicyName()).thenReturn("SIGNUP");

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InactivePointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));

        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("정책 포인트 null 값 -> InvalidPointPolicyException 발생")
    void calculate_nullPoint_throws() {
        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPolicyIsActive()).thenReturn(true);
        when(policy.getPolicyAddPoint()).thenReturn(null);
        when(policy.getPolicyId()).thenReturn(1);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InvalidPointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));

        verify(repository).findByPolicyName("SIGNUP");
    }

    @Test
    @DisplayName("정책 포인트 음수 값 -> InvalidPointPolicyException 발생")
    void calculate_negativePoint_throws() {
        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPolicyIsActive()).thenReturn(true);
        when(policy.getPolicyAddPoint()).thenReturn(-10);
        when(policy.getPolicyId()).thenReturn(1);

        when(repository.findByPolicyName("SIGNUP"))
                .thenReturn(Optional.of(policy));

        assertThrows(InvalidPointPolicyException.class,
                () -> helper.calculateByReason(PointReason.SIGNUP));

        verify(repository).findByPolicyName("SIGNUP");
    }
}
