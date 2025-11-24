package com.example.book2onandonuserservice.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. JWT 인증 설정 (Authorize 버튼 생성)
        String jwtSchemeName = "JWT-Auth";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("로그인 후 발급받은 Access Token을 입력하세요."));

        // 2. API 정보 설정
        return new OpenAPI()
                .info(new Info()
                        .title("Book2OnAndOn User Service API")
                        .description("회원 서비스 API 명세서")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement) // 인증 적용
                .components(components);
    }
}