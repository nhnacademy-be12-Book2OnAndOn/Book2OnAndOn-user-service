package com.example.book2onandonuserservice.point.exception;

public class PointBalanceIntegrityException extends RuntimeException {
    public PointBalanceIntegrityException() {
        super("데이터베이스의 포인트 총합(latestTotal)과 개별 잔여 포인트(remaining_point)의 합이 일치하지 않습니다. 데이터 정합성 확인이 필요합니다.");
    }
}