package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;

public interface AuthService {
    //로컬 회원가입
    UserResponseDto signUp(LocalSignUpRequestDto request);

    //로컬 로그인
    TokenResponseDto login(LoginRequestDto request);

    //PAYCO 로그인
    TokenResponseDto loginWithPayco(PaycoLoginRequestDto request);
}
