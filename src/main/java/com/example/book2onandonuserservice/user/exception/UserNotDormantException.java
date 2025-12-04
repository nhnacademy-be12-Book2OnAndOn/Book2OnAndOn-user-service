package com.example.book2onandonuserservice.user.exception;

public class UserNotDormantException extends RuntimeException {
    public UserNotDormantException() {
        super("휴면 계정이 아닙니다.");
    }
}