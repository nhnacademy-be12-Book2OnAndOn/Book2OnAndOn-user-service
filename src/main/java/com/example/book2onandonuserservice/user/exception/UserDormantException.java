package com.example.book2onandonuserservice.user.exception;

public class UserDormantException extends RuntimeException {
    public UserDormantException() {
        super("휴면 상태인 계정입니다.");
    }
}
