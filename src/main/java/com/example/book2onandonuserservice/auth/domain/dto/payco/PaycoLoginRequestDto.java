package com.example.book2onandonuserservice.auth.domain.dto.payco;

import jakarta.validation.constraints.NotBlank;

public record PaycoLoginRequestDto(
        @NotBlank String code
) {
}
