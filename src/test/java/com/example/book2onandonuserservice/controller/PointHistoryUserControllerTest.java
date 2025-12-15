package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.global.GlobalExceptionHandler;
import com.example.book2onandonuserservice.point.controller.PointHistoryUserController;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnOrderPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.EarnReviewPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.RefundPointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.request.UsePointRequestDto;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointSummaryResponseDto;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointHistoryUserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PointHistoryUserControllerTest { // partially covered code 4개 남음

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private PointHistoryService pointHistoryService;

    private static final String HEADER = "X-User-Id";

    // ===== GET /users/me/points =====

    @Test
    @DisplayName("GET /users/me/points - type=EARN 조회")
    void getMyPointHistory_typeEarn_ok() throws Exception {
        Page<PointHistoryResponseDto> page = new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));
        given(pointHistoryService.getMyPointHistoryByType(anyLong(), any(), any())).willReturn(page);

        mockMvc.perform(get("/users/me/points")
                        .param("type", "EARN")
                        .header(HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/me/points - type=USE 조회")
    void getMyPointHistory_typeUse_ok() throws Exception {
        Page<PointHistoryResponseDto> page = new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));
        given(pointHistoryService.getMyPointHistoryByType(anyLong(), any(), any())).willReturn(page);

        mockMvc.perform(get("/users/me/points")
                        .param("type", "USE")
                        .header(HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/me/points - type 없이 전체 조회")
    void getMyPointHistory_withoutType_ok() throws Exception {
        Page<PointHistoryResponseDto> page = new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));
        given(pointHistoryService.getMyPointHistory(anyLong(), any())).willReturn(page);

        mockMvc.perform(get("/users/me/points")
                        .header(HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/me/points - 헤더 누락 -> 400")
    void getMyPointHistory_missingHeader_400() throws Exception {
        mockMvc.perform(get("/users/me/points")
                        .param("type", "EARN"))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /users/me/points/current =====

    @Test
    @DisplayName("GET /users/me/points/current - 정상")
    void getCurrentPoint_ok() throws Exception {
        given(pointHistoryService.getMyCurrentPoint(10L))
                .willReturn(new CurrentPointResponseDto(500));

        mockMvc.perform(get("/users/me/points/current")
                        .header(HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPoint").value(500));
    }

    // ===== POST /users/me/points/earn/signup =====

    @Test
    @DisplayName("POST /users/me/points/earn/signup - 정상")
    void earnSignupPoint_ok() throws Exception {
        given(pointHistoryService.earnSignupPoint(10L))
                .willReturn(new EarnPointResponseDto(100, 1100, PointReason.SIGNUP));

        mockMvc.perform(post("/users/me/points/earn/signup")
                        .header(HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changedPoint").value(100));
    }

    // ===== POST /users/me/points/earn/review =====

    @Test
    @DisplayName("POST /users/me/points/earn/review - 이미지 리뷰 성공")
    void earnReviewPoint_image_ok() throws Exception {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto(10L, 1L, true);

        given(pointHistoryService.earnReviewPoint(any()))
                .willReturn(new EarnPointResponseDto(300, 1300, PointReason.REVIEW));

        mockMvc.perform(post("/users/me/points/earn/review")
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users/me/points/earn/review - userId 불일치 -> 400")
    void earnReviewPoint_userIdMismatch_400() throws Exception {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto(999L, 1L, true);

        mockMvc.perform(post("/users/me/points/earn/review")
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users/me/points/earn/review - dto.userId=null (검증 실패 케이스 기대) -> 400")
    void earnReviewPoint_userIdNull_400() throws Exception {
        EarnReviewPointRequestDto req = new EarnReviewPointRequestDto(null, 1L, false);

        // 컨트롤러 단의 userId 검증에서 막히므로 서비스 스텁이 필요 없음
        mockMvc.perform(post("/users/me/points/earn/review")
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /users/me/points/earn/order, /use, /refund =====
    // 중복 경고 제거: 성공 케이스는 파라미터라이즈드로 통합

    static Stream<Arguments> okPostEndpoints() {
        return Stream.of(
                Arguments.of(
                        "/users/me/points/earn/order",
                        new EarnOrderPointRequestDto(10L, 100L, 10000, 0.01),
                        new EarnPointResponseDto(100, 1100, PointReason.ORDER)
                ),
                Arguments.of(
                        "/users/me/points/use",
                        new UsePointRequestDto(10L, 100L, 100, 200),
                        new EarnPointResponseDto(-100, 900, PointReason.USE)
                ),
                Arguments.of(
                        "/users/me/points/refund",
                        new RefundPointRequestDto(10L, 100L, 10L, null, null),
                        new EarnPointResponseDto(100, 1000, PointReason.REFUND)
                )
        );
    }

    @ParameterizedTest(name = "[OK] POST {0}")
    @MethodSource("okPostEndpoints")
    @DisplayName("POST (earn/order | use | refund) - 정상(200)")
    void postEndpoints_ok(String url, Object requestBody, EarnPointResponseDto response) throws Exception {
        if (url.contains("/earn/order")) {
            given(pointHistoryService.earnOrderPoint(any())).willReturn(response);
        } else if (url.contains("/use")) {
            given(pointHistoryService.usePoint(any())).willReturn(response);
        } else if (url.contains("/refund")) {
            given(pointHistoryService.refundPoint(any())).willReturn(response);
        }

        mockMvc.perform(post(url)
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());
    }

    static Stream<Arguments> badRequestBodies_userIdMismatch() {
        return Stream.of(
                Arguments.of("/users/me/points/earn/order", new EarnOrderPointRequestDto(999L, 100L, 10000, 0.01)),
                Arguments.of("/users/me/points/use", new UsePointRequestDto(999L, 100L, 100, 200)),
                Arguments.of("/users/me/points/refund", new RefundPointRequestDto(999L, 100L, 10L, null, null))
        );
    }

    @ParameterizedTest(name = "[400] POST {0} userId mismatch")
    @MethodSource("badRequestBodies_userIdMismatch")
    @DisplayName("POST (earn/order | use | refund) - userId 불일치 -> 400")
    void postEndpoints_userIdMismatch_400(String url, Object requestBody) throws Exception {
        mockMvc.perform(post(url)
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> badRequestBodies_userIdNull() {
        return Stream.of(
                Arguments.of("/users/me/points/earn/order", new EarnOrderPointRequestDto(null, 100L, 10000, 0.01)),
                Arguments.of("/users/me/points/use", new UsePointRequestDto(null, 100L, 100, 200)),
                Arguments.of("/users/me/points/refund", new RefundPointRequestDto(null, 100L, 10L, null, null))
        );
    }

    @ParameterizedTest(name = "[400] POST {0} userId null")
    @MethodSource("badRequestBodies_userIdNull")
    @DisplayName("POST (earn/order | use | refund) - dto.userId=null -> 400")
    void postEndpoints_userIdNull_400(String url, Object requestBody) throws Exception {
        mockMvc.perform(post(url)
                        .header(HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /expiring, /summary =====

    @Test
    @DisplayName("GET /users/me/points/expiring - 정상")
    void getExpiring_ok() throws Exception {
        given(pointHistoryService.getExpiringPoints(anyLong(), anyInt()))
                .willReturn(new ExpiringPointResponseDto(50, LocalDateTime.now(), LocalDateTime.now().plusDays(7)));

        mockMvc.perform(get("/users/me/points/expiring")
                        .header(HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/me/points/summary - 정상")
    void getSummary_ok() throws Exception {
        given(pointHistoryService.getMyPointSummary(10L))
                .willReturn(new PointSummaryResponseDto(
                        1000, 300, 100, 50,
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now()
                ));

        mockMvc.perform(get("/users/me/points/summary")
                        .header(HEADER, 10L))
                .andExpect(status().isOk());
    }
}
