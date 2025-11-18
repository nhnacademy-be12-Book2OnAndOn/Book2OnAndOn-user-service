package com.example.book2onandonuserservice.auth.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LocalSignUpRequestDto(
        @NotBlank(message = "로그인 아이디는 필수입니다.")
        @Size(min = 4, max = 30, message = "최소 4자, 최대 30자 이내로 작성해주세요.")
        String userLoginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
                message = "비밀번호는 8~16자, 영문, 숫자, 특수문자를 포함해야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 10, message = "최대 10자 이내로 작성해주세요")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식으로 작성해주세요.")
        @Size(max = 30, message = "30자 이내로 작성해주세요.")
        String email,

        @NotBlank(message = "연락처는 필수입니다.")
        @Size(max = 11, message = "전화번호는 11자 이내로 작성해주세요.")
        String phone,

        LocalDate birth
) {
}
