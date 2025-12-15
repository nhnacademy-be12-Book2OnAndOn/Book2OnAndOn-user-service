package com.example.book2onandonuserservice.point.exception;

public class AdminAdjustPointNegativeBalanceException extends RuntimeException {
    public AdminAdjustPointNegativeBalanceException(int latestTotal, int amount) {
        super("조정 후 포인트가 음수가 될 수 없습니다. latestTotal=" + latestTotal + ", amount=" + amount);
    }
}