package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindIdRequestDto(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
