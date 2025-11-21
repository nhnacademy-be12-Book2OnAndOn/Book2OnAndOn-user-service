package com.example.book2onandonuserservice.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 320)
        String email,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 20자 이내여야 합니다.")
        String nickname,

        @NotBlank(message = "연락처는 필수입니다.")
        @Pattern(regexp = "^\\d{11}$", message = "연락처는 '-' 없이 11자리 숫자여야 합니다.")
        String phone
) {
}