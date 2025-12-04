package com.example.book2onandonuserservice.exception;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.global.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // 400
    @Test
    void handleValidationExceptions_returns400() throws Exception {
        String body = "{}";  // name 없음 → 검증 실패

        mockMvc.perform(post("/test/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INVALID_INPUT")))
                .andExpect(jsonPath("$.message", is("이름은 필수입니다.")));
    }

    // 400 테스트
    @Test
    void handleBadRequestExceptions_returns400() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("유효하지 않은 포인트 정책입니다. 정책명 = 잘못된 정책")));
    }

    // 404 테스트
    @Test
    void handleNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message").exists());
    }

    // 401 테스트
    @Test
    void handleAuthFailed_returns401() throws Exception {
        mockMvc.perform(get("/test/auth-failed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("AUTH_FAILED")))
                .andExpect(jsonPath("$.message").exists());
    }

    // 409 테스트
    @Test
    void handleSignupPointAlreadyGranted_returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is(
                        "해당 사용자는 이미 회원가입 포인트를 적립한 상태입니다. userId = 1"
                )));
    }


    // 500 테스트
    @Test
    void handleServerError_returns500() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("서버 오류가 발생했습니다.")));
    }

}