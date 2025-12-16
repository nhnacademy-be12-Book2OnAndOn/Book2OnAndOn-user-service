//package com.example.book2onandonuserservice.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.example.book2onandonuserservice.auth.controller.AuthController;
//import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
//import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
//import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
//import com.example.book2onandonuserservice.auth.service.AuthService;
//import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(AuthController.class)
//@WithMockUser
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AuthService authService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private RabbitTemplate rabbitTemplate;
//
//    @Test
//    @DisplayName("로컬 회원가입 성공 (201 Created)")
//    void localSignUp_Success() throws Exception {
//        LocalSignUpRequestDto request = new LocalSignUpRequestDto(
//                "testUser", "password123!", "홍길동",
//                "test@test.com", "testNickname", "01012345678", null
//        );
//
//        UserResponseDto response = UserResponseDto.builder()
//                .userId(1L)
//                .userLoginId("testUser")
//                .name("홍길동")
//                .nickname("testNickname")
//                .build();
//
//        given(authService.signUp(any(LocalSignUpRequestDto.class))).willReturn(response);
//
//        mockMvc.perform(post("/auth/signup")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.userLoginId").value("testUser"))
//                .andExpect(jsonPath("$.name").value("홍길동"))
//                .andExpect(jsonPath("$.nickname").value("testNickname"));
//    }
//
//    @Test
//    @DisplayName("로컬 로그인 성공 (200 OK)")
//    void localLogin_Success() throws Exception {
//        LoginRequestDto request = new LoginRequestDto("testUser", "password123!");
//        TokenResponseDto tokenResponse = new TokenResponseDto("access-token", "refresh-token", "Bearer", 3600L);
//
//        given(authService.login(any(LoginRequestDto.class))).willReturn(tokenResponse);
//
//        mockMvc.perform(post("/auth/login")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("access-token"))
//                .andExpect(jsonPath("$.tokenType").value("Bearer"));
//    }
//
//    @Test
//    @DisplayName("Payco 로그인 성공 (200 OK)")
//    void paycoLogin_Success() throws Exception {
//        PaycoLoginRequestDto request = new PaycoLoginRequestDto("payco-auth-code");
//        TokenResponseDto tokenResponse = new TokenResponseDto("payco-acc", "payco-ref", "Bearer", 3600L);
//
//        given(authService.loginWithPayco(any(PaycoLoginRequestDto.class))).willReturn(tokenResponse);
//
//        mockMvc.perform(post("/auth/login/payco")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("payco-acc"));
//    }
//
//    @Test
//    @DisplayName("로그아웃 성공")
//    void logout_Success() throws Exception {
//        String accessToken = "Bearer my-secret-token";
//
//        mockMvc.perform(post("/auth/logout")
//                        .with(csrf())
//                        .header("Authorization", accessToken))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(authService).logout("my-secret-token");
//    }
//
//    @Test
//    @DisplayName("아이디 찾기 성공")
//    void findId_Success() throws Exception {
//        FindIdRequestDto request = new FindIdRequestDto("홍길동", "test@test.com");
//        FindIdResponseDto response = new FindIdResponseDto("testUs**");
//
//        given(authService.findMemberIdByNameAndEmail(any(FindIdRequestDto.class))).willReturn(response);
//
//        mockMvc.perform(post("/auth/find-id")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userLoginId").value("testUs**"));
//    }
//
//    @Test
//    @DisplayName("비밀번호 찾기 (임시 비밀번호 발급)")
//    void findPassword_Success() throws Exception {
//        FindPasswordRequestDto request = new FindPasswordRequestDto("testUser", "test@test.com");
//
//        mockMvc.perform(post("/auth/find-password")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(authService).issueTemporaryPassword(any(FindPasswordRequestDto.class));
//    }
//
//    @Test
//    @DisplayName("이메일 인증코드 발송 요청")
//    void sendEmailVerification_Success() throws Exception {
//        String email = "test@test.com";
//
//        mockMvc.perform(post("/auth/email/send")
//                        .with(csrf())
//                        .param("email", email))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(authService).sendVerificationCode(email);
//    }
//
//    @Test
//    @DisplayName("이메일 인증코드 확인 - 성공")
//    void verifyEmail_Success() throws Exception {
//        String email = "test@test.com";
//        String code = "123456";
//
//        given(authService.verifyEmail(email, code)).willReturn(true);
//
//        mockMvc.perform(post("/auth/email/verify")
//                        .with(csrf())
//                        .param("email", email)
//                        .param("code", code))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string("인증 성공"));
//    }
//
//    @Test
//    @DisplayName("이메일 인증코드 확인 - 실패 (400 Bad Request)")
//    void verifyEmail_Fail() throws Exception {
//        String email = "test@test.com";
//        String code = "wrongCode";
//
//        given(authService.verifyEmail(email, code)).willReturn(false);
//
//        mockMvc.perform(post("/auth/email/verify")
//                        .with(csrf())
//                        .param("email", email)
//                        .param("code", code))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("인증 실패"));
//    }
//
//    // 휴면 해제 인증번호 발송
//    @Test
//    @DisplayName("휴면 해제 인증번호 발송 성공 (200 OK)")
//    void sendDormantVerification_Success() throws Exception {
//        String email = "dormant@test.com";
//
//        mockMvc.perform(post("/auth/dormant/email/send")
//                        .with(csrf())
//                        .param("email", email))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        verify(authService).sendDormantVerificationCode(email);
//    }
//
//    // 휴면 해제 처리 성공
//    @Test
//    @DisplayName("휴면 해제 성공 (200 OK)")
//    void unlockDormantAccount_Success() throws Exception {
//        String email = "dormant@test.com";
//        String code = "123456";
//
//        mockMvc.perform(post("/auth/dormant/unlock")
//                        .with(csrf())
//                        .param("email", email)
//                        .param("code", code))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string("휴면 상태가 해제되었습니다."));
//
//        verify(authService).unlockDormantAccount(email, code);
//    }
//
//}
