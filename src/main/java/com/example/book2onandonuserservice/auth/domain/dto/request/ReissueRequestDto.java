package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

//AccessToken 재발급 로직
public record ReissueRequestDto(
        String accessToken,
        @NotBlank String refreshToken
) {
}
