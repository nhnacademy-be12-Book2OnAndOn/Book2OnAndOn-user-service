package com.example.book2onandonuserservice.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) //JWT에서 CSRF 토큰이 필요없기 때문에 비활성화
                .formLogin(formLogin -> formLogin.disable()) //기본 로그인폼 페이지 비활성화
                .httpBasic(basic -> basic.disable()) //헤더에 ID/PW 직접보내는 Basic 인증 비활성화
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //세션 사용하지 않도록 설정
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 API 요청 허용
        return http.build();
    }
}
