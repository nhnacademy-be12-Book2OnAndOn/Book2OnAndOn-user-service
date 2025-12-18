package com.example.book2onandonuserservice.user.controller;

import com.example.book2onandonuserservice.global.annotation.AuthCheck;
import com.example.book2onandonuserservice.user.domain.dto.request.AdminUserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    // 대시보드 회원 수 조회
    @GetMapping("/count")
    @AuthCheck({Role.MEMBER_ADMIN, Role.BOOK_ADMIN, Role.COUPON_ADMIN, Role.ORDER_ADMIN})
    public ResponseEntity<Long> countUsers() {
        Long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }


    //전체 회원 목록 조회
    @GetMapping
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    //특정 회원 상세 조회
    @GetMapping("/{userId}")
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<UserResponseDto> getUserDetail(@PathVariable Long userId) {
        UserResponseDto userInfo = userService.getMyInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    //회원 정보 수정 (권한, 상태 등급 등)
    @PutMapping("/{userId}")
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUserUpdateRequestDto request
    ) {
        userService.updateUserByAdmin(userId, request);
        return ResponseEntity.ok().build();
    }

    //회원 강제탈퇴(soft Delete)
    @DeleteMapping("/{userId}")
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<Void> deleteUserByAdmin(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "관리자 직권 탈퇴") String reason
    ) {
        userService.deleteUserByAdmin(userId, reason);
        return ResponseEntity.noContent().build();
    }
}
