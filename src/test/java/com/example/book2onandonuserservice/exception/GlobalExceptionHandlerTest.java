package com.example.book2onandonuserservice.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.global.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    // [400] Validation 에러 테스트
    @Test
    @DisplayName("Validation 실패 시 400 반환 (INVALID_INPUT)")
    void handleValidationExceptions_returns400() throws Exception {
        String body = "{}";  // 필수 필드 누락

        mockMvc.perform(post("/test/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_INPUT"))) // 혹은 $.code (DTO 필드명에 따라 조정)
                .andExpect(jsonPath("$.message").exists());
    }

    // [400] 일반적인 Bad Request 테스트 (포인트 정책)
    @Test
    @DisplayName("InvalidPointPolicyException 발생 시 400 반환")
    void handleBadRequestExceptions_Point_returns400() throws Exception {
        mockMvc.perform(get("/test/bad-request-point"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("잘못된 포인트 정책")));
    }

    // [400] 유저 관련 Bad Request 테스트 (비밀번호)
    @Test
    @DisplayName("SameAsOldPasswordException 발생 시 400 반환")
    void handleBadRequestExceptions_Password_returns400() throws Exception {
        mockMvc.perform(get("/test/bad-request-password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.")));
    }

    // [401] 인증 실패 테스트
    @Test
    @DisplayName("인증 실패 시 401 반환")
    void handleAuthFailed_returns401() throws Exception {
        mockMvc.perform(get("/test/auth-failed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("AUTH_FAILED")))
                .andExpect(jsonPath("$.message").exists());
    }

    // [403] 권한 없음/휴면 테스트
    @Test
    @DisplayName("휴면 계정 접속 시 403 반환")
    void handleForbidden_returns403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("ACCOUNT_DORMANT")));
    }

    // [404] 회원 찾기 실패
    @Test
    @DisplayName("존재하지 않는 회원 조회 시 404 반환")
    void handleNotFound_User_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("해당 사용자를 찾을 수 없습니다")));
    }

    // [404] 등급 찾기 실패 (통합된 핸들러 테스트)
    @Test
    @DisplayName("존재하지 않는 등급 조회 시 404 반환")
    void handleNotFound_Grade_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found-grade"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("존재하지 않는 등급입니다.")));
    }

    // [409] 충돌 테스트
    @Test
    @DisplayName("중복 가입 포인트 지급 시 409 반환")
    void handleConflict_returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("이미 가입 포인트를 받았습니다.")));
    }

    // [500] 서버 내부 오류 테스트
    @Test
    @DisplayName("서버 내부 오류 발생 시 500 반환")
    void handleServerError_returns500() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("서버 오류가 발생했습니다.")));
    }

    // [502] Bad Gateway 테스트
    @Test
    @DisplayName("Payco 서버 오류 시 502 반환")
    void handleBadGateway_returns502() throws Exception {
        mockMvc.perform(get("/test/bad-gateway"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error", is("PAYCO_BAD_GATEWAY")));
    }
}