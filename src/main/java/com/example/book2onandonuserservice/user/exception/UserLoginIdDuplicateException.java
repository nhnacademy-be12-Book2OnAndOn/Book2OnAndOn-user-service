package com.example.book2onandonuserservice.user.exception;

public class UserLoginIdDuplicateException extends RuntimeException {
    public UserLoginIdDuplicateException() {
        super("이미 사용중인 아이디입니다.");
    }
}
