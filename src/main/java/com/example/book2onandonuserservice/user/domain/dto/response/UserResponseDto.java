package com.example.book2onandonuserservice.user.domain.dto.response;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record UserResponseDto(
        Long userId,
        String userLoginId,
        String name,
        String email,
        String phone,
        String nickname,
        Role role,
        String gradeName,
        String status,
        String provider
) {
    public static UserResponseDto fromEntity(Users user) {
        String providerStr = "LOCAL";
        if (user.getUserAuths() != null && !user.getUserAuths().isEmpty()) {
            providerStr = user.getUserAuths().stream()
                    .map(UserAuth::getProvider)
                    .collect(Collectors.joining(", "));
        }

        return UserResponseDto.builder()
                .userId(user.getUserId())
                .userLoginId(user.getUserLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .gradeName(user.getUserGrade().getGradeName().name())
                .status(user.getStatus().name())
                .provider(providerStr)
                .build();
    }
}