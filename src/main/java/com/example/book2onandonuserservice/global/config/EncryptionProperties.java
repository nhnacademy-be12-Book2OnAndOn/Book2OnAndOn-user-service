package com.example.book2onandonuserservice.global.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {
    private Map<String, String> keys;

    private String activeVersion;

    private String hashSecret;
}