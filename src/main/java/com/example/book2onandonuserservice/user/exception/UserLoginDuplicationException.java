package com.example.book2onandonuserservice.user.exception;

public class UserLoginDuplicationException extends RuntimeException {
    public UserLoginDuplicationException() {
        super("이미 사용중인 아이디 입니다.");
    }
}
