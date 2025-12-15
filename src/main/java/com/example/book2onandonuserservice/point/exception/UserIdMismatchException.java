package com.example.book2onandonuserservice.point.exception;

public class UserIdMismatchException extends RuntimeException {
    public UserIdMismatchException(Long userId) {
        super("요청 userId와 인증된 사용자가 일치하지 않습니다. userId = " + userId);
    }

    public UserIdMismatchException(Long orderId, Long userId, Long historyUserId) {
        super("해당 주문의 포인트 사용 이력과 요청 userId가 일치하지 않습니다. " +
                "orderId=" + orderId + ", historyUserId=" + historyUserId + ", requestUserId=" + userId);
    }
}
