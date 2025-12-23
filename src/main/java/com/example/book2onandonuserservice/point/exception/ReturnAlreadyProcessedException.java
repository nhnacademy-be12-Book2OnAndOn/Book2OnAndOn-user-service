package com.example.book2onandonuserservice.point.exception;

public class ReturnAlreadyProcessedException extends RuntimeException {
    public ReturnAlreadyProcessedException(Long refundId) {
        super("이미 처리된 반품입니다. refundId = " + refundId);
    }
}
