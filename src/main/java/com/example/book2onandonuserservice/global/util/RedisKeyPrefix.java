package com.example.book2onandonuserservice.global.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKeyPrefix {
    REFRESH_TOKEN("user:rt:"),
    BLACKLIST("user:logout:"),
    EMAIL_CODE("user:email:code:"),
    EMAIL_DORMANT_CODE("user:email_dormant:code:"),
    EMAIL_VERIFIED("user:email:verified:");

    private final String prefix;

    public String buildKey(String identifier) {
        return this.prefix + identifier;
    }

}
