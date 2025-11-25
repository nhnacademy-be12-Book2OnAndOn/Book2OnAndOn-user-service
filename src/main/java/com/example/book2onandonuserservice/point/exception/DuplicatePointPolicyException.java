package com.example.book2onandonuserservice.point.exception;

public class DuplicatePointPolicyException extends RuntimeException {

    public DuplicatePointPolicyException(String name) {
        super("포인트 정책이 중복 적용되었습니다. 정책명 = " + name);
    }
}
