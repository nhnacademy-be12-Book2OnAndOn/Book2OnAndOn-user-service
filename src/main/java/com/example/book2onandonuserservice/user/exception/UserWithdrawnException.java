package com.example.book2onandonuserservice.user.exception;

public class UserWithdrawnException extends RuntimeException {
    public UserWithdrawnException() {
        super("탈퇴한 회원입니다.");
    }
}
