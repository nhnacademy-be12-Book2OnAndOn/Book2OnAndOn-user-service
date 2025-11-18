package com.example.book2onandonuserservice.user.exception;

public class UserEmailDuplicationException extends RuntimeException {
    public UserEmailDuplicationException() {
        super("이미 가입중인 이메일 입니다.");
    }
}
