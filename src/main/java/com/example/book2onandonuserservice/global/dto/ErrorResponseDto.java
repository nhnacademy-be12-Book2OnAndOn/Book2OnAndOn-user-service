package com.example.book2onandonuserservice.global.dto;

public record ErrorResponseDto(
        String code,    // 예: "BAD_REQUEST", "NOT_FOUND"
        String message  // 예: "이미 사용중인 아이디입니다."
) {
}
