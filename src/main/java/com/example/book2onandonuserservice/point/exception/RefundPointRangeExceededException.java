package com.example.book2onandonuserservice.point.exception;

public class RefundPointRangeExceededException extends RuntimeException {
    public RefundPointRangeExceededException(int requested, int actual) {
        super("복구 요청 포인트가 실제 사용 포인트를 초과합니다. requested=" + requested + ", actual=" + actual);
    }
}