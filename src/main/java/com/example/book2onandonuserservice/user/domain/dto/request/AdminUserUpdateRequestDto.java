package com.example.book2onandonuserservice.user.domain.dto.request;

public record AdminUserUpdateRequestDto(
        String role,
        String status,
        String gradeName
) {
}