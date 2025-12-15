package com.example.book2onandonuserservice.user.exception;

// 404
public class GradeNotFoundException extends RuntimeException {

    public GradeNotFoundException() {
        super("존재하지 않는 등급입니다.");
    }

    public GradeNotFoundException(Long gradeId) {
        super("존재하지 않는 등급입니다. ID: " + gradeId);
    }
}