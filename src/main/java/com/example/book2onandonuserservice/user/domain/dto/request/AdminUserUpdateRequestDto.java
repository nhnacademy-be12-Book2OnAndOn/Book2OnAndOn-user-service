package com.example.book2onandonuserservice.user.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserUpdateRequestDto {
    private String role;
    private String status;
    private String gradeName;
}