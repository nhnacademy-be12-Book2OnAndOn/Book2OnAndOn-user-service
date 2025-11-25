package com.example.book2onandonuserservice.point.exception;

public class InvalidAuthenticationException extends RuntimeException {
    public InvalidAuthenticationException(Long userId) {
        super("유효하지 않은 사용자 인증입니다. userId = " + userId);
    }
}
