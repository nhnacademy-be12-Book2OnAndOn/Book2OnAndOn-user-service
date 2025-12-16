package com.example.book2onandonuserservice.controller;

import static com.example.book2onandonuserservice.user.domain.entity.GradeName.GOLD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.user.controller.UserGradeController;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeCreateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserGradeResponseDto;
import com.example.book2onandonuserservice.user.service.UserGradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserGradeController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserGradeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserGradeService userGradeService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    // 등급 생성
    @Test
    @DisplayName("등급 생성 성공")
    void createGrade_success() throws Exception {
        UserGradeCreateRequestDto request =
                new UserGradeCreateRequestDto(GOLD, 0.03, 3000);

        UserGradeResponseDto response =
                new UserGradeResponseDto(1L, "GOLD", 0.03, 3000);

        when(userGradeService.createGrade(any())).thenReturn(response);

        mockMvc.perform(post("/admin/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gradeId").value(1L))
                .andExpect(jsonPath("$.gradeName").value("GOLD"))
                .andExpect(jsonPath("$.pointAddRate").value(0.03)) // ★ 수정
                .andExpect(jsonPath("$.pointCutline").value(3000)); // ★ 필드명 통일

        verify(userGradeService).createGrade(any());
    }


    // 등급 전체 조회
    @Test
    @DisplayName("전체 등급 조회 성공")
    void getAllGrades_success() throws Exception {
        List<UserGradeResponseDto> list = List.of(
                new UserGradeResponseDto(1L, "BASIC", 0.01, 0),
                new UserGradeResponseDto(2L, "GOLD", 0.03, 3000)
        );

        when(userGradeService.getAllGrades()).thenReturn(list);

        mockMvc.perform(get("/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gradeName").value("BASIC"))
                .andExpect(jsonPath("$[1].gradeName").value("GOLD"));

        verify(userGradeService).getAllGrades();
    }

    // 등급 정보 수정
    @Test
    @DisplayName("등급 수정 성공")
    void updateGrade_success() throws Exception {
        Long gradeId = 2L;

        UserGradeUpdateRequestDto request =
                new UserGradeUpdateRequestDto(0.05, 5000);

        UserGradeResponseDto response =
                new UserGradeResponseDto(gradeId, "GOLD", 0.05, 5000);

        when(userGradeService.updateGrade(eq(gradeId), any()))
                .thenReturn(response);

        mockMvc.perform(put("/admin/grades/{gradeId}", gradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gradeId").value(2L))
                .andExpect(jsonPath("$.pointAddRate").value(0.05))   // ★ 수정
                .andExpect(jsonPath("$.pointCutline").value(5000));  // ★ 필드명 통일

        verify(userGradeService).updateGrade(eq(gradeId), any());
    }
}
