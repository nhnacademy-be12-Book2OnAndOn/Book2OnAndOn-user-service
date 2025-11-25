package com.example.book2onandonuserservice.auth.controller;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //로컬 회원가입
    //POST /auth/signup
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> localSignUp(
            @Valid @RequestBody LocalSignUpRequestDto request
    ) {
        UserResponseDto response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //로컬 로그인
    //POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> localLogin(
            @Valid @RequestBody LoginRequestDto request
    ) {
        TokenResponseDto tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    //Payco 로그인 및 가입
    @PostMapping("/login/payco")
    public ResponseEntity<TokenResponseDto> paycoLogin(
            @Valid @RequestBody PaycoLoginRequestDto request
    ) {
        TokenResponseDto tokenResponse = authService.loginWithPayco(request);
        return ResponseEntity.ok(tokenResponse);
    }
}
