package com.example.book2onandonuserservice.user.domain.dto.response;

import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import lombok.Builder;

@Builder
public record UserResponseDto(
        Long userId,
        String userLoginId, // 로그인 아이디
        String name,
        String email,
        String phone,
        String nickname,
        Role role,
        String gradeName
) {
    public static UserResponseDto fromEntity(Users user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .userLoginId(user.getUserLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .gradeName(user.getUserGrade().getGradeName().name())
                .build();
    }
}
