package com.example.book2onandonuserservice.point.exception;

public class PointAlreadyUsedForOrderException extends RuntimeException {

    public PointAlreadyUsedForOrderException(Long orderId) {
        super("이미 포인트 사용이 처리된 주문입니다. orderId = " + orderId);
    }
}
