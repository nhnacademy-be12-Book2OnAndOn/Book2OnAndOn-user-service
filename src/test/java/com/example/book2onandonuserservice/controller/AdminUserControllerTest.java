package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.user.controller.AdminUserController;
import com.example.book2onandonuserservice.user.domain.dto.request.AdminUserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    // 전체 사용자 조회
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getAllUsers_success() throws Exception {

        UserResponseDto user1 = new UserResponseDto(
                1L,
                "login1",
                "홍길동",
                "test1@mail.com",
                "01011112222",
                "nick1",
                Role.USER,
                "BASIC",
                "ACTIVE"
        );

        UserResponseDto user2 = new UserResponseDto(
                2L,
                "login2",
                "김철수",
                "test2@mail.com",
                "01033334444",
                "nick2",
                Role.USER,
                "BASIC",
                "ACTIVE"
        );

        Page<UserResponseDto> page =
                new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);

        when(userService.getAllUsers(any())).thenReturn(page);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(1L))
                .andExpect(jsonPath("$.content[1].userId").value(2L));

        verify(userService).getAllUsers(any());
    }

    //  특정 사용자 상세 조회
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getUserDetail_success() throws Exception {

        Long userId = 10L;

        UserResponseDto dto = new UserResponseDto(
                userId,
                "login10",
                "홍길동",
                "test1@mail.com",
                "01011112222",
                "nick1",
                Role.USER,
                "BASIC",
                "ACTIVE"
        );

        when(userService.getMyInfo(userId)).thenReturn(dto);

        mockMvc.perform(get("/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("홍길동"));

        verify(userService).getMyInfo(userId);
    }

    //  회원 정보 수정
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateUser_success() throws Exception {

        Long id = 5L;

        AdminUserUpdateRequestDto request =
                new AdminUserUpdateRequestDto("SUPER_ADMIN", "ACTIVE", "GOLD");

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/admin/users/{userId}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(userService).updateUserByAdmin(eq(id), any());
    }


    // 회원 삭제
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void deleteUserByAdmin_success() throws Exception {
        Long id = 7L;

        mockMvc.perform(delete("/admin/users/{userId}", id)
                        .with(csrf())
                        .param("reason", "테스트 삭제"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserByAdmin(id, "테스트 삭제");
    }
}
