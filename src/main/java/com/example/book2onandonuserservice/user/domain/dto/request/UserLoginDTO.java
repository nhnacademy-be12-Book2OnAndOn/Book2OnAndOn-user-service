package com.example.book2onandonuserservice.user.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank
    private String userLoginId;

    @NotBlank
    //길이: 8자리 이상 16자리 이하
    // 영문 + 숫자 + 특수문자 포함
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-za-z\\d@$!%*#?&]{8,16}$")
    private String password;
}
