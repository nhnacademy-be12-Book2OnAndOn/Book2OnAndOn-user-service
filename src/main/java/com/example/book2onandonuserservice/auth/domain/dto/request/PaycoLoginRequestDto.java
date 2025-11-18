package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record PaycoLoginRequestDto(
        @NotBlank String providerId,
        @NotBlank String providerName,
        String email,
        LocalDate birthday,
        String phone,
        String name
) {
}
