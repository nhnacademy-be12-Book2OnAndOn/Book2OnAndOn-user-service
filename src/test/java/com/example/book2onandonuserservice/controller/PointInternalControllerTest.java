package com.example.book2onandonuserservice.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.point.controller.PointInternalController;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.EarnOrderPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.EarnReviewPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.RefundPointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.internal.UsePointInternalRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class PointInternalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PointHistoryService pointHistoryService;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("[POST] /internal/users/{userId}/points/earn/review - 내부 리뷰 적립: path userId가 service DTO에 세팅된다")
    void earnReviewPoint_ok_andMapsUserId() throws Exception {
        Long pathUserId = 10L;

        EarnReviewPointInternalRequestDto req = new EarnReviewPointInternalRequestDto();
        req.setReviewId(777L);
        req.setHasImage(true);

        given(pointHistoryService.earnReviewPoint(any(EarnReviewPointRequestDto.class)))
                .willReturn(new EarnPointResponseDto(300, 1300, PointReason.REVIEW));

        mockMvc.perform(post("/internal/users/{userId}/points/earn/review", pathUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changedPoint").value(300))
                .andExpect(jsonPath("$.earnReason").value("REVIEW"))
                .andExpect(jsonPath("$.totalPointAfter").value(1300));

        ArgumentCaptor<EarnReviewPointRequestDto> captor =
                forClass(EarnReviewPointRequestDto.class);
        verify(pointHistoryService).earnReviewPoint(captor.capture());

        EarnReviewPointRequestDto mapped = captor.getValue();
        assertThat(mapped.getUserId()).isEqualTo(pathUserId);
        assertThat(mapped.getReviewId()).isEqualTo(777L);
        assertThat(mapped.isHasImage()).isTrue();
    }

    @Test
    @DisplayName("[POST] /internal/users/{userId}/points/earn/order - 내부 주문 적립: path userId가 service DTO에 세팅된다")
    void earnOrderPoint_ok_andMapsFields() throws Exception {
        Long pathUserId = 10L;

        EarnOrderPointInternalRequestDto req = new EarnOrderPointInternalRequestDto();
        req.setOrderId(1000L);
        req.setPureAmount(10000);
        req.setPointAddRate(0.01);

        given(pointHistoryService.earnOrderPoint(any(EarnOrderPointRequestDto.class)))
                .willReturn(new EarnPointResponseDto(100, 1100, PointReason.ORDER));

        mockMvc.perform(post("/internal/users/{userId}/points/earn/order", pathUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changedPoint").value(100))
                .andExpect(jsonPath("$.earnReason").value("ORDER"))
                .andExpect(jsonPath("$.totalPointAfter").value(1100));

        ArgumentCaptor<EarnOrderPointRequestDto> captor =
                forClass(EarnOrderPointRequestDto.class);
        verify(pointHistoryService).earnOrderPoint(captor.capture());

        EarnOrderPointRequestDto mapped = captor.getValue();
        assertThat(mapped.getUserId()).isEqualTo(pathUserId);
        assertThat(mapped.getOrderId()).isEqualTo(1000L);
        assertThat(mapped.getPureAmount()).isEqualTo(10000);
        assertThat(mapped.getPointAddRate()).isEqualTo(0.01);
    }

    @Test
    @DisplayName("[POST] /internal/users/{userId}/points/use - 내부 포인트 사용: path userId가 service DTO에 세팅된다")
    void usePoint_ok_andMapsFields() throws Exception {
        Long pathUserId = 10L;

        UsePointInternalRequestDto req = new UsePointInternalRequestDto();
        req.setOrderId(1000L);
        req.setUseAmount(200);
        req.setAllowedMaxUseAmount(300);

        given(pointHistoryService.usePoint(any(UsePointRequestDto.class)))
                .willReturn(new EarnPointResponseDto(-200, 800, PointReason.USE));

        mockMvc.perform(post("/internal/users/{userId}/points/use", pathUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changedPoint").value(-200))
                .andExpect(jsonPath("$.earnReason").value("USE"))
                .andExpect(jsonPath("$.totalPointAfter").value(800));

        ArgumentCaptor<UsePointRequestDto> captor =
                forClass(UsePointRequestDto.class);
        verify(pointHistoryService).usePoint(captor.capture());

        UsePointRequestDto mapped = captor.getValue();
        assertThat(mapped.getUserId()).isEqualTo(pathUserId);
        assertThat(mapped.getOrderId()).isEqualTo(1000L);
        assertThat(mapped.getUseAmount()).isEqualTo(200);
        assertThat(mapped.getAllowedMaxUseAmount()).isEqualTo(300);
    }

    @Test
    @DisplayName("[POST] /internal/users/{userId}/points/refund - 내부 포인트 반환: path userId가 service DTO에 세팅된다")
    void refundPoint_ok_andMapsFields() throws Exception {
        Long pathUserId = 10L;

        RefundPointInternalRequestDto req = new RefundPointInternalRequestDto();
        req.setOrderId(1000L);
        req.setRefundId(55L);
        req.setUsedPoint(200);
        req.setRefundAmount(15000);

        given(pointHistoryService.refundPoint(any(RefundPointRequestDto.class)))
                .willReturn(new EarnPointResponseDto(200, 1000, PointReason.REFUND));

        mockMvc.perform(post("/internal/users/{userId}/points/refund", pathUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changedPoint").value(200))
                .andExpect(jsonPath("$.earnReason").value("REFUND"))
                .andExpect(jsonPath("$.totalPointAfter").value(1000));

        ArgumentCaptor<RefundPointRequestDto> captor =
                forClass(RefundPointRequestDto.class);
        verify(pointHistoryService).refundPoint(captor.capture());

        RefundPointRequestDto mapped = captor.getValue();
        assertThat(mapped.getUserId()).isEqualTo(pathUserId);
        assertThat(mapped.getOrderId()).isEqualTo(1000L);
        assertThat(mapped.getRefundId()).isEqualTo(55L);
        assertThat(mapped.getUsedPoint()).isEqualTo(200);
        assertThat(mapped.getRefundAmount()).isEqualTo(15000);
    }

    @Test
    @DisplayName("[POST] /internal/users/{userId}/points/earn/review - 서비스가 1회 호출된다")
    void earnReviewPoint_callsServiceOnce() throws Exception {
        Long pathUserId = 10L;

        EarnReviewPointInternalRequestDto req = new EarnReviewPointInternalRequestDto();
        req.setReviewId(777L);
        req.setHasImage(false);

        given(pointHistoryService.earnReviewPoint(any(EarnReviewPointRequestDto.class)))
                .willReturn(new EarnPointResponseDto(200, 1200, PointReason.REVIEW));

        mockMvc.perform(post("/internal/users/{userId}/points/earn/review", pathUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(pointHistoryService, Mockito.times(1))
                .earnReviewPoint(any(EarnReviewPointRequestDto.class));
    }
}
