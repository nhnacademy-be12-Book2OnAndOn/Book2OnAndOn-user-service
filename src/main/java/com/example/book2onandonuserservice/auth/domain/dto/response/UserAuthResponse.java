package com.example.book2onandonuserservice.auth.domain.dto.response;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import lombok.Builder;

@Builder
public record UserAuthResponse(
        Long authId,
        String provider
) {
    public static UserAuthResponse fromEntity(UserAuth auth) {
        return UserAuthResponse.builder()
                .authId(auth.getAuthId())
                .provider(auth.getProvider())
                .build();
    }
}
