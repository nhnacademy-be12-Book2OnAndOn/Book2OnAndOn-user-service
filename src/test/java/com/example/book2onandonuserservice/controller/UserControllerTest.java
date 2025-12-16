package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
import com.example.book2onandonuserservice.global.dto.MyLikedBookResponseDto;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.user.controller.UserController;
import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = {
        "ENC_SECRET_KEY=testsecretkey123456789012345678",
        "ENC_IV=1234567890123456",
        "JWT_SECRET=testjwtsecretkey12345678901234567890"
})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    EncryptionUtils encryptionUtils;

    @MockBean
    EncryptStringConverter encryptStringConverter;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    AuthService authService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;


    @Test
    @DisplayName("GET /users/me - 내 정보 조회 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void getMyInfo_success() throws Exception {
        UserResponseDto dto = new UserResponseDto(
                1L,
                "loginId",
                "홍길동",
                "test@mail.com",
                "01011112222",
                "nick",
                Role.USER,
                "BASIC",
                "ACTIVE",
                "local"
        );

        Mockito.when(userService.getMyInfo(1L)).thenReturn(dto);

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }


    @Test
    @DisplayName("PUT /users/me - 내 정보 수정 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void updateMyInfo_success() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "새이름", "new@mail.com", "newNick", "01099998888"
        );

        UserResponseDto response = new UserResponseDto(
                1L,
                "loginId",
                "새이름",
                "new@mail.com",
                "01099998888",
                "newNick",
                Role.USER,
                "BASIC",
                "ACTIVE",
                "local"
        );

        Mockito.when(userService.updateMyInfo(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/users/me")
                        .header("X-User-Id", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.email").value("new@mail.com"));
    }


    @Test
    @DisplayName("PUT /users/me/password - 비밀번호 변경 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void changePassword_success() throws Exception {
        PasswordChangeRequestDto request =
                new PasswordChangeRequestDto("oldPass123!", "NewPass123!");

        mockMvc.perform(put("/users/me/password")
                        .header("X-User-Id", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).changePassword(eq(1L), any());
    }


    @Test
    @DisplayName("DELETE /users/me - 회원 탈퇴 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteMyUser_success() throws Exception {

        mockMvc.perform(delete("/users/me")
                        .header("X-User-Id", 1L)
                        .with(csrf())
                        .content("테스트 탈퇴"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(eq(1L), any());
    }


    @Test
    @DisplayName("GET /users/{userId}/reviews - 리뷰 조회 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void getUserReviews_success() throws Exception {

        BookReviewResponseDto review = new BookReviewResponseDto();

        Page<BookReviewResponseDto> page =
                new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        Mockito.when(userService.getUserReviews(eq(1L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/users/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        Mockito.verify(userService).getUserReviews(eq(1L), any());
    }

    // [추가] 좋아요 목록 조회
    @Test
    @DisplayName("GET /users/me/likes - 좋아요 목록 조회 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void getMyLikedBooks_success() throws Exception {
        MyLikedBookResponseDto likedBook = new MyLikedBookResponseDto(); // 기본 생성자 사용
        Page<MyLikedBookResponseDto> page =
                new PageImpl<>(List.of(likedBook), PageRequest.of(0, 10), 1);

        Mockito.when(userService.getMyLikedBooks(eq(1L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/users/me/likes")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        Mockito.verify(userService).getMyLikedBooks(eq(1L), any());
    }

    // [추가] 닉네임 중복 확인
    @Test
    @DisplayName("GET /users/check-nickname - 닉네임 중복 확인 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void checkNickname_success() throws Exception {
        String nickname = "testNick";
        Mockito.when(userService.checkNickname(nickname)).thenReturn(true);

        mockMvc.perform(get("/users/check-nickname")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        Mockito.verify(userService).checkNickname(nickname);
    }

    // [추가] 아이디 중복 확인
    @Test
    @DisplayName("GET /users/check-id - 아이디 중복 확인 성공")
    @WithMockUser(username = "user", roles = {"USER"})
    void checkLoginId_success() throws Exception {
        String userLoginId = "testId";
        Mockito.when(userService.checkLoginId(userLoginId)).thenReturn(false);

        mockMvc.perform(get("/users/check-id")
                        .param("userLoginId", userLoginId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        Mockito.verify(userService).checkLoginId(userLoginId);
    }
}