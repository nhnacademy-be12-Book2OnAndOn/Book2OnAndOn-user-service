package com.example.book2onandonuserservice.auth.domain.dto.payco;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaycoTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") String expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}
