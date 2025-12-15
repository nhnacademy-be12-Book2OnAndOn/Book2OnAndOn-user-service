package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;

public interface AuthService {

    //이메일 인증번호 발송
    void sendVerificationCode(String email);

    //휴면 해제용 인증번호 발송
    void sendDormantVerificationCode(String email);

    //인증번호 검증
    boolean verifyEmail(String email, String code);

    //휴면 해제 처리 매서드
    void unlockDormantAccount(String email, String code);

    //로컬 회원가입
    UserResponseDto signUp(LocalSignUpRequestDto request);

    //로컬 로그인
    TokenResponseDto login(LoginRequestDto request);

    //PAYCO 로그인
    TokenResponseDto loginWithPayco(PaycoLoginRequestDto request);

    //로그아웃
    void logout(String accessToken);

    //아이디찾기
    FindIdResponseDto findMemberIdByNameAndEmail(FindIdRequestDto request);

    //임시비밀번호
    void issueTemporaryPassword(FindPasswordRequestDto request);
}
