package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDto(
        @NotBlank String userId,
        @NotBlank(message = "비밀번호는 필수 입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
                message = "비밀번호는 8~16자, 영문, 숫자, 특수문자를 포함해야 합니다.")
        String password) {
}
