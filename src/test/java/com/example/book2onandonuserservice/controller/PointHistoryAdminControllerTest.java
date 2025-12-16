//package com.example.book2onandonuserservice.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.example.book2onandonuserservice.point.controller.PointHistoryAdminController;
//import com.example.book2onandonuserservice.point.domain.dto.request.PointHistoryAdminAdjustRequestDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
//import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
//import com.example.book2onandonuserservice.point.service.PointHistoryService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(PointHistoryAdminController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class PointHistoryAdminControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @MockBean
//    PointHistoryService pointHistoryService;
//
//    @Test
//    @DisplayName("GET /admin/points?userId=... : 특정 유저 포인트 이력 페이지 조회")
//    void getUserPointHistory_ok() throws Exception {
//        long userId = 1L;
//
//        Page<PointHistoryResponseDto> page =
//                new PageImpl<>(List.of(Mockito.mock(PointHistoryResponseDto.class)));
//
//        given(pointHistoryService.getMyPointHistory(eq(userId), any(Pageable.class)))
//                .willReturn(page);
//
//        mockMvc.perform(get("/admin/points")
//                        .param("userId", String.valueOf(userId))
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).getMyPointHistory(eq(userId), any(Pageable.class));
//    }
//
//    @Test
//    @DisplayName("GET /admin/points/current?userId=... : 특정 유저 현재 포인트 조회")
//    void getUserCurrentPoint_ok() throws Exception {
//        long userId = 1L;
//
//        given(pointHistoryService.getMyCurrentPoint(userId))
//                .willReturn(Mockito.mock(CurrentPointResponseDto.class));
//
//        mockMvc.perform(get("/admin/points/current")
//                        .param("userId", String.valueOf(userId)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).getMyCurrentPoint(userId);
//    }
//
//    @Test
//    @DisplayName("POST /admin/points/adjust : 관리자 수동 지급/차감")
//    void adjustPointByAdmin_ok() throws Exception {
//        PointHistoryAdminAdjustRequestDto req = Mockito.mock(PointHistoryAdminAdjustRequestDto.class);
//
//        given(pointHistoryService.adjustPointByAdmin(any(PointHistoryAdminAdjustRequestDto.class)))
//                .willReturn(Mockito.mock(EarnPointResponseDto.class));
//
//        mockMvc.perform(post("/admin/points/adjust")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isOk());
//
//        verify(pointHistoryService).adjustPointByAdmin(any(PointHistoryAdminAdjustRequestDto.class));
//    }
//}
