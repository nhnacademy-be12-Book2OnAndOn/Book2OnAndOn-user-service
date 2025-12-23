package com.example.book2onandonuserservice.user.exception;

public class MissingRequiredFieldException extends RuntimeException {

    public MissingRequiredFieldException(String fieldName) {
        super("필수 값이 누락되었습니다. field = " + fieldName);
    }

}
