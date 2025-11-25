package com.example.book2onandonuserservice.user.exception;

//400
public class GradeNameDuplicateException extends RuntimeException {
    public GradeNameDuplicateException(String gradeName) {
        super("이미 존재하는 등급입니다. " + gradeName);
    }
}
