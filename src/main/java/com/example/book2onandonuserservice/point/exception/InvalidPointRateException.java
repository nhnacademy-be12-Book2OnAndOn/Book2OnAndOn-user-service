package com.example.book2onandonuserservice.point.exception;

public class InvalidPointRateException extends RuntimeException {
    public InvalidPointRateException(Double rate) {
        super("등급 적립률은 0보다 커야 합니다. gradeRate = " + rate);
    }
}
