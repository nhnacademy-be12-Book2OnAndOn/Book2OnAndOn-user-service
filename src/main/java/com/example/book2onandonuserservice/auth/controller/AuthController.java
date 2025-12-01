package com.example.book2onandonuserservice.auth.controller;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok().build();
    }

    //아이디찾기
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponseDto> findId(@Valid @RequestBody FindIdRequestDto request) {
        FindIdResponseDto response = authService.findId(request);
        return ResponseEntity.ok(response);
    }

    //비밀번호 찾기(임시 비밀번호 발급)
    @PostMapping("/find-password")
    public ResponseEntity<Void> findPassword(@Valid @RequestBody FindPasswordRequestDto request) {
        authService.issueTemporaryPassword(request);
        return ResponseEntity.ok().build();
    }

}
