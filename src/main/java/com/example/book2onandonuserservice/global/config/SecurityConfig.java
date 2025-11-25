package com.example.book2onandonuserservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //패스워드 인코더를 빈으로 등록 -> 서비스단에서 RequiredArgsConstructor로 주입받을 수 있음
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //보안필터 체인 설정
    //요청은 허용, 세션은 끔(인증은 Gateway에서 처리)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) //JWT에서는 불필요한 CSRF 보안을 끔
                .formLogin(AbstractHttpConfigurer::disable) //기본 로그인폼을 끔
                .httpBasic(AbstractHttpConfigurer::disable) //HTTP Basic 인증을 끔
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .permitAll()); //모든 요청 허용(인증은 Gateway)

        return http.build();
    }

}
