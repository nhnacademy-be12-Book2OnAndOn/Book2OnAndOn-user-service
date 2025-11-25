package com.example.book2onandonuserservice.point.exception;

public class InactivePointPolicyException extends RuntimeException {
    public InactivePointPolicyException(String name) {
        super("정책이 비활성화 상태입니다. name = " + name);
    }
}
