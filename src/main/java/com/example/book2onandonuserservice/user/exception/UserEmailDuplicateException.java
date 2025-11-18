package com.example.book2onandonuserservice.user.exception;

public class UserEmailDuplicateException extends RuntimeException {
    public UserEmailDuplicateException() {
        super("이미 가입된 이메일입니다.");
    }
}
