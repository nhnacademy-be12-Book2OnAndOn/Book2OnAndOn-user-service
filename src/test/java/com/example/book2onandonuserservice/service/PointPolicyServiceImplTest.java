package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.repository.PointPolicyRepository;
import com.example.book2onandonuserservice.point.service.PointPolicyServiceImpl;
import com.example.book2onandonuserservice.point.support.pointPolicy.PointPolicyValidator;
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
                .policyAddRate(0.1)
                .policyAddPoint(null)
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
        assertEquals(0.1, list.getFirst().getPointAddRate());
    }


    // 단건 조회
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


    // 정책 비율/포인트 수정
    @Test
    void updatePolicyRateAndPoint_success_rate() {

        PointPolicy policyData = new PointPolicy(1, "ORDER", 0.05, null, true);
        when(pointPolicyRepository.findById(1)).thenReturn(Optional.of(policyData));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddRate(0.1);

        PointPolicyResponseDto result =
                pointPolicyService.updatePolicyRateAndPoint(1, req);

        assertEquals(0.1, result.getPointAddRate());
        assertNull(result.getPointAddPoint());
    }


    @Test
    void updatePolicyRateAndPoint_success_point() {

        PointPolicy policyData = new PointPolicy(1, "ORDER", null, 50, true);
        when(pointPolicyRepository.findById(1)).thenReturn(Optional.of(policyData));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddPoint(100);

        PointPolicyResponseDto result =
                pointPolicyService.updatePolicyRateAndPoint(1, req);

        assertEquals(100, result.getPointAddPoint());
        assertNull(result.getPointAddRate());
    }


    @Test
    void updatePolicyRateAndPoint_notFound() {
        when(pointPolicyRepository.findById(999))
                .thenReturn(Optional.empty());

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddRate(0.1);

        assertThrows(PointPolicyNotFoundException.class,
                () -> pointPolicyService.updatePolicyRateAndPoint(999, req));
    }


    @Test
    void updatePolicyRateAndPoint_invalid_both_set() {

        PointPolicy policyData = new PointPolicy(1, "ORDER", 0.05, null, true);
        when(pointPolicyRepository.findById(1)).thenReturn(Optional.of(policyData));

        PointPolicyUpdateRequestDto req = new PointPolicyUpdateRequestDto();
        req.setPointAddRate(0.1);
        req.setPointAddPoint(100);

        assertThrows(InvalidPointPolicyException.class,
                () -> pointPolicyService.updatePolicyRateAndPoint(1, req));
    }


    // 정책 활성/비활성
    @Test
    void updatePolicyActive_success() {

        PointPolicyActiveUpdateRequestDto req =
                new PointPolicyActiveUpdateRequestDto(false);

        when(pointPolicyRepository.findById(1))
                .thenReturn(Optional.of(policy));

        PointPolicyResponseDto dto =
                pointPolicyService.updatePolicyActive(1, req);

        assertFalse(policy.getPolicyIsActive());
        assertEquals(false, dto.getPointIsActive());
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
