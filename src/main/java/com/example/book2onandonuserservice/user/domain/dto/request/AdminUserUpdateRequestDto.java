package com.example.book2onandonuserservice.user.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminUserUpdateRequestDto {
    private String nickname;
    private String role;
}
