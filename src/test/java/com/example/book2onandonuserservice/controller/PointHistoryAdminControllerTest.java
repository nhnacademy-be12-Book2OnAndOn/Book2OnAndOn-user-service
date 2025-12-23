package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.point.controller.PointHistoryAdminController;
import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointHistoryAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PointHistoryAdminControllerTest {

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PointHistoryService pointHistoryService;

    @Test
    @DisplayName("[GET] /admin/points - 특정 유저 포인트 이력 페이지 조회: 200 OK")
    void getUserPointHistory_ok() throws Exception {
        long userId = 1L;

        Page<PointHistoryResponseDto> page =
                new PageImpl<>(List.of(Mockito.mock(PointHistoryResponseDto.class)));

        given(pointHistoryService.getMyPointHistory(eq(userId), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/admin/points")
                        .param("userId", String.valueOf(userId))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(pointHistoryService).getMyPointHistory(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("[GET] /admin/points/current - 특정 유저 현재 포인트 조회: 200 OK")
    void getUserCurrentPoint_ok() throws Exception {
        long userId = 1L;

        given(pointHistoryService.getMyCurrentPoint(userId))
                .willReturn(new CurrentPointResponseDto(1000));

        mockMvc.perform(get("/admin/points/current")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());

        verify(pointHistoryService).getMyCurrentPoint(userId);
    }

    @Test
    @DisplayName("[POST] /admin/points/adjust - 관리자 수동 지급/차감: 200 OK")
    void adjustPointByAdmin_ok() throws Exception {
        // mock DTO를 JSON으로 보내면 {}가 될 수 있어 검증(@Valid)에서 400이 뜰 수 있음
        // -> 실제 객체로 필수 필드 채워서 200 보장
        PointHistoryAdminAdjustRequestDto req = new PointHistoryAdminAdjustRequestDto();
        req.setUserId(1L);
        req.setAmount(100);
        req.setMemo("테스트 지급");

        given(pointHistoryService.adjustPointByAdmin(any(PointHistoryAdminAdjustRequestDto.class)))
                .willReturn(new EarnPointResponseDto(100, 1100, PointReason.ADMIN_ADJUST));

        mockMvc.perform(post("/admin/points/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(pointHistoryService).adjustPointByAdmin(any(PointHistoryAdminAdjustRequestDto.class));
    }
}
