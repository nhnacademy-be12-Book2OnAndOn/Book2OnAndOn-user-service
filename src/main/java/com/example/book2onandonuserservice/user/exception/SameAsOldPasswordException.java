package com.example.book2onandonuserservice.user.exception;

public class SameAsOldPasswordException extends RuntimeException {
    public SameAsOldPasswordException() {
        super("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }
}