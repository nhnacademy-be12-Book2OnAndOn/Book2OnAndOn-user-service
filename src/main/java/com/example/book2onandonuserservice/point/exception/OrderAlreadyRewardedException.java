package com.example.book2onandonuserservice.point.exception;

public class OrderAlreadyRewardedException extends RuntimeException {
    public OrderAlreadyRewardedException(Long orderItemId) {
        super("이미 해당 주문에 대해 적립이 완료되었습니다. orderItemId =" + orderItemId);
    }
}
