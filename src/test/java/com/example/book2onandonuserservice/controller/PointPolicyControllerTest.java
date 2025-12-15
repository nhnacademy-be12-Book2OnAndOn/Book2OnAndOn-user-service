package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.point.controller.PointPolicyController;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyActiveUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.PointPolicyUpdateRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointPolicyResponseDto;
import com.example.book2onandonuserservice.point.service.PointPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PointPolicyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PointPolicyService pointPolicyService;

    private static final String USER_ID_HEADER = "X-User-Id";

    @Test
    @DisplayName("전체 정책 조회 성공")
    void getAllPoliciesTest() throws Exception {

        List<PointPolicyResponseDto> dtoList = List.of(
                PointPolicyResponseDto.builder()
                        .pointPolicyId(1)
                        .pointPolicyName("SIGNUP")
                        .pointAddPoint(500)
                        .pointIsActive(true)
                        .build(),
                PointPolicyResponseDto.builder()
                        .pointPolicyId(2)
                        .pointPolicyName("REVIEW_TEXT")
                        .pointAddPoint(200)
                        .pointIsActive(true)
                        .build()
        );

        Mockito.when(pointPolicyService.getAllPolicies()).thenReturn(dtoList);

        mockMvc.perform(get("/admin/point-policies")
                        .header(USER_ID_HEADER, 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pointPolicyName").value("SIGNUP"));
    }

    @Test
    @DisplayName("단건 정책 조회 성공")
    void getPolicyTest() throws Exception {

        PointPolicyResponseDto dto = PointPolicyResponseDto.builder()
                .pointPolicyId(1)
                .pointPolicyName("SIGNUP")
                .pointAddPoint(500)
                .pointIsActive(true)
                .build();

        Mockito.when(pointPolicyService.getPolicyByName("SIGNUP")).thenReturn(dto);

        mockMvc.perform(get("/admin/point-policies/SIGNUP")
                        .header(USER_ID_HEADER, 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointPolicyName").value("SIGNUP"))
                .andExpect(jsonPath("$.pointAddPoint").value(500));
    }

    @Test
    @DisplayName("정책 Point 수정 성공")
    void updatePolicyPointTest() throws Exception {

        PointPolicyUpdateRequestDto requestDto = new PointPolicyUpdateRequestDto();
        requestDto.setPointAddPoint(300);

        PointPolicyResponseDto updatedDto = PointPolicyResponseDto.builder()
                .pointPolicyId(1)
                .pointPolicyName("SIGNUP")
                .pointAddPoint(300)
                .pointIsActive(true)
                .build();

        Mockito.when(pointPolicyService.updatePolicyPoint(eq(1), any(PointPolicyUpdateRequestDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/admin/point-policies/1")
                        .header(USER_ID_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointAddPoint").value(300));
    }

    @Test
    @DisplayName("정책 활성/비활성 PATCH 성공")
    void updatePolicyActiveTest() throws Exception {

        PointPolicyActiveUpdateRequestDto requestDto =
                new PointPolicyActiveUpdateRequestDto(false);

        PointPolicyResponseDto updatedDto = PointPolicyResponseDto.builder()
                .pointPolicyId(1)
                .pointPolicyName("SIGNUP")
                .pointAddPoint(500)
                .pointIsActive(false)
                .build();

        Mockito.when(pointPolicyService.updatePolicyActive(eq(1), any(PointPolicyActiveUpdateRequestDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/point-policies/1/active")
                        .header(USER_ID_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointIsActive").value(false));
    }
}
