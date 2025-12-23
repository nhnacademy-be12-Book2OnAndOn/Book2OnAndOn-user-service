package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.global.GlobalExceptionHandler;
import com.example.book2onandonuserservice.point.controller.PointHistoryUserController;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.ExpiringPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointSummaryResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointHistoryUserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PointHistoryUserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private PointHistoryService pointHistoryService;

    private static final String HEADER = "X-User-Id";

    @Nested
    @DisplayName("GET /users/me/points")
    class GetMyPointHistory {

        @Test
        @DisplayName("type 없이 전체 조회: 200 OK + Page 응답")
        void getMyPointHistory_withoutType_ok() throws Exception {
            Page<PointHistoryResponseDto> page =
                    new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));

            given(pointHistoryService.getMyPointHistory(eq(10L), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/users/me/points")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            verify(pointHistoryService).getMyPointHistory(eq(10L), any(Pageable.class));
        }

        @Test
        @DisplayName("type=EARN 조회: 200 OK + Page 응답")
        void getMyPointHistory_typeEarn_ok() throws Exception {
            Page<PointHistoryResponseDto> page =
                    new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));

            given(pointHistoryService.getMyPointHistoryByType(eq(10L), eq("EARN"), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/users/me/points")
                            .param("type", "EARN")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            verify(pointHistoryService).getMyPointHistoryByType(eq(10L), eq("EARN"), any(Pageable.class));
        }

        @Test
        @DisplayName("type=USE 조회: 200 OK + Page 응답")
        void getMyPointHistory_typeUse_ok() throws Exception {
            Page<PointHistoryResponseDto> page =
                    new PageImpl<>(List.of(PointHistoryResponseDto.builder().build()));

            given(pointHistoryService.getMyPointHistoryByType(eq(10L), eq("USE"), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/users/me/points")
                            .param("type", "USE")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));

            verify(pointHistoryService).getMyPointHistoryByType(eq(10L), eq("USE"), any(Pageable.class));
        }

        @Test
        @DisplayName("헤더 누락: 400 Bad Request")
        void getMyPointHistory_missingHeader_400() throws Exception {
            mockMvc.perform(get("/users/me/points"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /users/me/points/current")
    class GetCurrentPoint {

        @Test
        @DisplayName("현재 포인트 조회: 200 OK + currentPoint 반환")
        void getCurrentPoint_ok() throws Exception {
            given(pointHistoryService.getMyCurrentPoint(10L))
                    .willReturn(new CurrentPointResponseDto(500));

            mockMvc.perform(get("/users/me/points/current")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPoint").value(500));

            verify(pointHistoryService).getMyCurrentPoint(10L);
        }
    }

    @Nested
    @DisplayName("GET /users/me/points/expiring")
    class GetExpiringPoints {

        @Test
        @DisplayName("소멸 예정 포인트 조회 기본값(days=7): 200 OK")
        void getExpiring_defaultDays_ok() throws Exception {
            given(pointHistoryService.getExpiringPoints(10L, 7))
                    .willReturn(new ExpiringPointResponseDto(
                            50,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusDays(7)
                    ));

            mockMvc.perform(get("/users/me/points/expiring")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expiringAmount").value(50));

            verify(pointHistoryService).getExpiringPoints(10L, 7);
        }

        @Test
        @DisplayName("소멸 예정 포인트 조회 days 파라미터 지정: 200 OK")
        void getExpiring_customDays_ok() throws Exception {
            given(pointHistoryService.getExpiringPoints(10L, 30))
                    .willReturn(new ExpiringPointResponseDto(
                            0,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusDays(30)
                    ));

            mockMvc.perform(get("/users/me/points/expiring")
                            .param("days", "30")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expiringAmount").value(0));

            verify(pointHistoryService).getExpiringPoints(10L, 30);
        }
    }

    @Nested
    @DisplayName("GET /users/me/points/summary")
    class GetSummary {

        @Test
        @DisplayName("포인트 요약 조회: 200 OK")
        void getSummary_ok() throws Exception {
            given(pointHistoryService.getMyPointSummary(10L))
                    .willReturn(new PointSummaryResponseDto(
                            1000, 300, 100, 50,
                            LocalDateTime.now().minusDays(30),
                            LocalDateTime.now()
                    ));

            mockMvc.perform(get("/users/me/points/summary")
                            .header(HEADER, 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPoint").value(1000))
                    .andExpect(jsonPath("$.earnedThisMonth").value(300))
                    .andExpect(jsonPath("$.usedThisMonth").value(100))
                    .andExpect(jsonPath("$.expiringSoon").value(50))
                    .andExpect(jsonPath("$.from").exists())
                    .andExpect(jsonPath("$.to").exists());

            verify(pointHistoryService).getMyPointSummary(10L);
        }
    }
}
