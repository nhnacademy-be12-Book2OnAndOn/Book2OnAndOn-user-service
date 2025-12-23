package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.service.PointPolicyServiceImpl;
import com.example.book2onandonuserservice.point.support.pointpolicy.PointPolicyValidator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointPolicyServiceImplTest {

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @Mock
    private PointPolicyValidator pointPolicyValidator;

    @InjectMocks
    private PointPolicyServiceImpl pointPolicyService;

    private PointPolicy policy;

    @BeforeEach
    void setup() {
        policy = PointPolicy.builder()
                .policyId(1)
                .policyName("REVIEW_TEXT")
                .policyAddPoint(100)
                .policyIsActive(true)
                .build();
    }

    // ====================================================
    // 1. 전체 조회
    // ====================================================
    @Test
    @DisplayName("getAllPolicies: 전체 정책을 DTO로 변환해 반환한다")
    void getAllPolicies_success() {
        when(pointPolicyRepository.findAll())
                .thenReturn(List.of(policy));

        List<PointPolicyResponseDto> list = pointPolicyService.getAllPolicies();

        assertEquals(1, list.size());
        assertEquals("REVIEW_TEXT", list.get(0).getPointPolicyName()); // Java 17 안전
        assertEquals(100, list.get(0).getPointAddPoint());

        verify(pointPolicyRepository).findAll();
        verifyNoInteractions(pointPolicyValidator);
    }

    // ====================================================
    // 2. 단건 조회
    // ====================================================
    @Test
    @DisplayName("getPolicyByName: 정책명이 존재하면 DTO를 반환한다")
    void getPolicyByName_success() {
        when(pointPolicyRepository.findByPolicyName("REVIEW_TEXT"))
                .thenReturn(Optional.of(policy));

        PointPolicyResponseDto dto = pointPolicyService.getPolicyByName("REVIEW_TEXT");

        assertAll(
                () -> assertEquals(1, dto.getPointPolicyId()),
                () -> assertEquals("REVIEW_TEXT", dto.getPointPolicyName())
        );

        verify(pointPolicyRepository).findByPolicyName("REVIEW_TEXT");
        verifyNoInteractions(pointPolicyValidator);
    }

    @Test
    @DisplayName("getPolicyByName: 정책명이 없으면 PointPolicyNotFoundException")
    void getPolicyByName_notFound() {
        when(pointPolicyRepository.findByPolicyName("XXX"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.getPolicyByName("XXX"));

        verify(pointPolicyRepository).findByPolicyName("XXX");
        verifyNoInteractions(pointPolicyValidator);
    }

    // ====================================================
    // 3. 포인트 정책 수정
    // ====================================================
    @Test
    @DisplayName("updatePolicyPoint: 정책이 존재하고 값이 유효하면 포인트를 변경한다")
    void updatePolicyPoint_success() {
        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(200);

        PointPolicyResponseDto result = pointPolicyService.updatePolicyPoint(1, req);

        assertAll(
                () -> assertEquals(200, result.getPointAddPoint()),
                () -> assertEquals(200, policy.getPolicyAddPoint()) // 엔티티 상태 변경 확인
        );

        verify(pointPolicyRepository).findById(1);
        verify(pointPolicyValidator).validatePoint(200);
    }

    @Test
    @DisplayName("updatePolicyPoint: 정책이 없으면 PointPolicyNotFoundException")
    void updatePolicyPoint_notFound() {
        when(pointPolicyRepository.findById(999))
                .thenReturn(Optional.empty());

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(100);

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.updatePolicyPoint(999, req));

        verify(pointPolicyRepository).findById(999);
        verifyNoInteractions(pointPolicyValidator);
    }

    @Test
    @DisplayName("updatePolicyPoint: validator에서 예외가 나면 그대로 전파한다")
    void updatePolicyPoint_invalidPoint() {
        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(-10);

        doThrow(new InvalidPointPolicyException("Invalid point"))
                .when(pointPolicyValidator)
                .validatePoint(-10);

        assertThrows(InvalidPointPolicyException.class,
                () -> pointPolicyService.updatePolicyPoint(1, req));

        verify(pointPolicyRepository).findById(1);
        verify(pointPolicyValidator).validatePoint(-10);
        // 정책 값이 변경되지 않았는지도 확인(부작용 방지)
        assertEquals(100, policy.getPolicyAddPoint());
    }

    // ====================================================
    // 4. 활성/비활성 수정
    // ====================================================
    @Test
    @DisplayName("updatePolicyActive: 정책이 존재하면 활성 상태를 변경한다")
    void updatePolicyActive_success() {
        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyActiveUpdateRequestDto req = new PointPolicyActiveUpdateRequestDto(false);

        PointPolicyResponseDto dto = pointPolicyService.updatePolicyActive(1, req);

        assertAll(
                () -> assertFalse(policy.getPolicyIsActive()),
                () -> assertFalse(dto.getPointIsActive())
        );

        verify(pointPolicyRepository).findById(1);
        verifyNoInteractions(pointPolicyValidator);
    }

    @Test
    @DisplayName("updatePolicyActive: 정책이 없으면 PointPolicyNotFoundException")
    void updatePolicyActive_notFound() {
        when(pointPolicyRepository.findById(99))
                .thenReturn(Optional.empty());

        PointPolicyActiveUpdateRequestDto req = new PointPolicyActiveUpdateRequestDto(true);

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.updatePolicyActive(99, req));

        verify(pointPolicyRepository).findById(99);
        verifyNoInteractions(pointPolicyValidator);
    }
}
