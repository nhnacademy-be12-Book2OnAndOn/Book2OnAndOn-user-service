package com.example.book2onandonuserservice.user.exception;

public class SuperAdminDeletionException extends RuntimeException {
    public SuperAdminDeletionException() {
        super("SUPER ADMIN 계정은 강제 탈퇴시킬 수 없습니다.");
    }
}