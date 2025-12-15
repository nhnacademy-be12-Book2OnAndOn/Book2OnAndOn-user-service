package com.example.book2onandonuserservice.auth.domain.dto.response;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType, //Bearer
        long expiresIn //유호시간
) {
}
