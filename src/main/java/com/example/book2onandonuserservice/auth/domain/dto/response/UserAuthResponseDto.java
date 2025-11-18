package com.example.book2onandonuserservice.auth.domain.dto.response;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import lombok.Builder;

@Builder
public record UserAuthResponseDto(
        Long authId,
        String provider
) {
    public static UserAuthResponseDto fromEntity(UserAuth auth) {
        return UserAuthResponseDto.builder()
                .authId(auth.getAuthId())
                .provider(auth.getProvider())
                .build();
    }
}
