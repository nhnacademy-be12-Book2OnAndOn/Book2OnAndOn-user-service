package com.example.book2onandonuserservice.user.exception;

public class UserNicknameDuplicationException extends RuntimeException {
    public UserNicknameDuplicationException() {
        super("이미 사용중인 닉네임 입니다.");
    }
}
