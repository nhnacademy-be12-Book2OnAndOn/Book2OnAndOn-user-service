package com.example.book2onandonuserservice.auth.exception;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() {
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
