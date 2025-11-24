package com.example.book2onandonuserservice.point.exception;

public class SignupPointAlreadyGrantedException extends RuntimeException {
    public SignupPointAlreadyGrantedException(Long userId) {
        super("해당 사용자는 이미 회원가입 포인트를 적립한 상태입니다. userId = " + userId);
    }
}
