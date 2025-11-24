package com.example.book2onandonuserservice.point.exception;

public class InsufficientPointException extends RuntimeException {
    public InsufficientPointException(int current, int requested) {
        super("보유 포인트보다 많이 사용할 수 없습니다. 현재=" + current + ", 요청=" + requested);
    }
}
