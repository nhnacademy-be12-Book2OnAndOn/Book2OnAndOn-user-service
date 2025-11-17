package com.example.book2onandonuserservice.user.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE = "해당 사용자를 찾을 수 없습니다.";

    public UserNotFoundException(Long userId) {
        super(MESSAGE + "ID" + userId);
    }
}
