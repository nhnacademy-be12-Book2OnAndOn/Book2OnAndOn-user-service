package com.example.book2onandonuserservice.point.exception;

public class UserIdMismatchException extends RuntimeException {
    public UserIdMismatchException(Long userId) {
        super("요청 userId와 인증된 사용자가 일치하지 않습니다. userId = " + userId);
    }
}
