package com.example.book2onandonuserservice.point.exception;

public class PointRangeExceededException extends RuntimeException {
    public PointRangeExceededException(int maxUseAmount) {
        super("포인트는 최대 " + maxUseAmount + "P까지 사용 가능합니다.");
    }
}
