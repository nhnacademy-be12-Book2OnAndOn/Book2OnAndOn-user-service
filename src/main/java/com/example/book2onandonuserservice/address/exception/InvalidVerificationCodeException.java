package com.example.book2onandonuserservice.address.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("인증번호가 일치하지 않거나 만료되었습니다.");
    }
}
