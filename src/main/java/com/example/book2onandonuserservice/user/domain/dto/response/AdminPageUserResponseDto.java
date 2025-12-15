package com.example.book2onandonuserservice.user.domain.dto.response;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class AdminPageUserResponseDto {
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private LocalDate birth;
    private String userLoginId;
    private String role;
    private String provider;

    public AdminPageUserResponseDto(Users users) {
        this.name = users.getName();
        this.nickname = users.getNickname();
        this.email = users.getEmail();
        this.phone = users.getPhone();
        this.birth = users.getBirth();
        this.userLoginId = users.getUserLoginId();
        this.role = users.getRole().name();
        this.provider = users.getUserAuths().stream()
                .map(UserAuth::getProvider)
                .collect(Collectors.joining(", "));
    }

}
