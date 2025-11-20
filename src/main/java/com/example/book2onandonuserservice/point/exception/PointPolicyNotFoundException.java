package com.example.book2onandonuserservice.point.exception;

public class PointPolicyNotFoundException extends RuntimeException {

    public PointPolicyNotFoundException(String name) {
        super("포인트 정책을 찾을 수 없습니다. 정책명 = " + name);
    }

    public PointPolicyNotFoundException(Long id) {
        super("포인트 정책을 찾을 수 없습니다. 정책 ID = " + id);
    }
}
