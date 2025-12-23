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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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

    // (프로젝트 환경상 Redis Bean이 컨텍스트에 필요해서 MockBean 유지)
    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    private static final String USER_ID_HEADER = "X-User-Id";

    @Nested
    @DisplayName("GET /admin/point-policies")
    class GetAllPolicies {

        @Test
        @DisplayName("전체 정책 조회 성공: 200 OK + 배열 반환")
        void success() throws Exception {
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
                    .andExpect(jsonPath("$[0].pointPolicyName").value("SIGNUP"))
                    .andExpect(jsonPath("$[1].pointPolicyName").value("REVIEW_TEXT"));
        }
    }

    @Nested
    @DisplayName("GET /admin/point-policies/{policyName}")
    class GetPolicy {

        @Test
        @DisplayName("단건 정책 조회 성공: 200 OK + 단건 DTO 반환")
        void success() throws Exception {
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
                    .andExpect(jsonPath("$.pointAddPoint").value(500))
                    .andExpect(jsonPath("$.pointIsActive").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /admin/point-policies/{policyId}")
    class UpdatePolicyPoint {

        @Test
        @DisplayName("정책 포인트 수정 성공: 200 OK + 변경된 pointAddPoint 반환")
        void success() throws Exception {
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
                    .andExpect(jsonPath("$.pointPolicyId").value(1))
                    .andExpect(jsonPath("$.pointPolicyName").value("SIGNUP"))
                    .andExpect(jsonPath("$.pointAddPoint").value(300))
                    .andExpect(jsonPath("$.pointIsActive").value(true));
        }

        @Test
        @DisplayName("정책 포인트 수정 실패(Validation): 요청 DTO가 유효하지 않으면 400 Bad Request")
        void validationFail_400() throws Exception {
            // NOTE:
            // 이 테스트는 PointPolicyUpdateRequestDto에 실제로 validation annotation이 붙어있다는 전제입니다.
            // 예: @NotNull, @Min(0) 등
            PointPolicyUpdateRequestDto invalid = new PointPolicyUpdateRequestDto();
            invalid.setPointAddPoint(null); // @NotNull 이라면 400

            mockMvc.perform(put("/admin/point-policies/1")
                            .header(USER_ID_HEADER, 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /admin/point-policies/{policyId}/active")
    class UpdatePolicyActive {

        @Test
        @DisplayName("정책 활성/비활성 수정 성공: 200 OK + pointIsActive 반영")
        void success() throws Exception {
            PointPolicyActiveUpdateRequestDto requestDto = new PointPolicyActiveUpdateRequestDto(false);

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
                    .andExpect(jsonPath("$.pointPolicyId").value(1))
                    .andExpect(jsonPath("$.pointIsActive").value(false));
        }
    }
}
