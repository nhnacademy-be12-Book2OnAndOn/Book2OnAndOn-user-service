package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
        @NotBlank String userLoginId,
        @Email @NotBlank String email
) {
}
