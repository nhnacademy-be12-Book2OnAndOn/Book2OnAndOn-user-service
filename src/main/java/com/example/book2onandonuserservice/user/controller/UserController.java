package com.example.book2onandonuserservice.user.controller;

import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private static final String USER_ID_HEADER = "X-User-Id";

    //내 정보 조회
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getMyInfo(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }

    //내 정보 수정
    @PutMapping("/users/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateMyInfo(userId, request));
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
    public ResponseEntity<Void> deleteMyUser(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody(required = false) String reason
    ) {
        String finalReason = (reason == null || reason.isBlank()) ? "사유 선택 안함" : reason;

        userService.deleteUser(userId, finalReason);
        return ResponseEntity.noContent().build();
    }

    //회원 리뷰 조회
    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<Page<BookReviewResponseDto>> getUserReviews(
            @PathVariable("userId") Long userId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<BookReviewResponseDto> reviews = userService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
