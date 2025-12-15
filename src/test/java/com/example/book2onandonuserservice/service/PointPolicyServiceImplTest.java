package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
    void getAllPolicies_success() {

        when(pointPolicyRepository.findAll())
                .thenReturn(List.of(policy));

        List<PointPolicyResponseDto> list = pointPolicyService.getAllPolicies();

        assertEquals(1, list.size());
        assertEquals("REVIEW_TEXT", list.getFirst().getPointPolicyName());
        assertEquals(100, list.getFirst().getPointAddPoint());
    }


    // ====================================================
    // 2. 단건 조회
    // ====================================================
    @Test
    void getPolicyByName_success() {
        when(pointPolicyRepository.findByPolicyName("REVIEW_TEXT"))
                .thenReturn(Optional.of(policy));

        PointPolicyResponseDto dto = pointPolicyService.getPolicyByName("REVIEW_TEXT");

        assertEquals(1, dto.getPointPolicyId());
        assertEquals("REVIEW_TEXT", dto.getPointPolicyName());
    }

    @Test
    void getPolicyByName_notFound() {
        when(pointPolicyRepository.findByPolicyName("XXX"))
                .thenReturn(Optional.empty());

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.getPolicyByName("XXX"));
    }


    // ====================================================
    // 3. 포인트 정책 수정
    // ====================================================
    @Test
    void updatePolicyPoint_success() {
        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(100);

        PointPolicyResponseDto result = pointPolicyService.updatePolicyPoint(1, req);

        assertEquals(100, result.getPointAddPoint());
    }


    @Test
    void updatePolicyPoint_notFound() {
        when(pointPolicyRepository.findById(999))
                .thenReturn(Optional.empty());

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(100);

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.updatePolicyPoint(999, req));
    }


    @Test
    void updatePolicyPoint_invalidPoint() {
        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(-10);

        doThrow(new InvalidPointPolicyException("Invalid point"))
                .when(pointPolicyValidator).validatePoint(any());

        assertThrows(InvalidPointPolicyException.class,
                () -> pointPolicyService.updatePolicyPoint(1, req));
    }


    // ====================================================
    // 4. 활성/비활성 수정
    // ====================================================
    @Test
    void updatePolicyActive_success() {
        when(pointPolicyRepository.findById(1)).thenReturn(Optional.of(policy));

        PointPolicyActiveUpdateRequestDto req = new PointPolicyActiveUpdateRequestDto(false);

        PointPolicyResponseDto dto = pointPolicyService.updatePolicyActive(1, req);

        assertFalse(policy.getPolicyIsActive());
        assertFalse(dto.getPointIsActive());
    }


    @Test
    void updatePolicyActive_notFound() {

        when(pointPolicyRepository.findById(99))
                .thenReturn(Optional.empty());

        PointPolicyActiveUpdateRequestDto req =
                new PointPolicyActiveUpdateRequestDto(true);

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.updatePolicyActive(99, req));
    }

}
