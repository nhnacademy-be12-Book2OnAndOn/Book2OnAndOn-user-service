package com.example.book2onandonuserservice.user.controller;

import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final String USER_ID_HEADER = "X-USER-ID";

    //내 정보 조회
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getMyInfo(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        UserResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    //내 정보 수정
    @PutMapping("/users/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        UserResponseDto response = userService.updateMyInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    //비밀번호 변경
    @PostMapping("/users/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody PasswordChangeRequestDto request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    //회원 탈퇴
    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    //(admin) 특정 회원 정보 조회
    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserInfoByAdmin(
            @PathVariable Long userId
    ) {
        UserResponseDto response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }
}
