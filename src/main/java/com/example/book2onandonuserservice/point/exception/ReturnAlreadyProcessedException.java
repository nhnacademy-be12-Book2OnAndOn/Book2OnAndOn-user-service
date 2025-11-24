package com.example.book2onandonuserservice.point.exception;

public class ReturnAlreadyProcessedException extends RuntimeException {
    public ReturnAlreadyProcessedException(Long returnId) {
        super("이미 처리된 반품입니다. returnId = " + returnId);
    }
}
