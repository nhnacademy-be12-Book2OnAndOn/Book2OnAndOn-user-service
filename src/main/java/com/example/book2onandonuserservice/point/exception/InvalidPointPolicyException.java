package com.example.book2onandonuserservice.point.exception;

public class InvalidPointPolicyException extends RuntimeException {
    public InvalidPointPolicyException(String name) {
        super("유효하지 않은 포인트 정책입니다. 정책명 = " + name);
    }
}
