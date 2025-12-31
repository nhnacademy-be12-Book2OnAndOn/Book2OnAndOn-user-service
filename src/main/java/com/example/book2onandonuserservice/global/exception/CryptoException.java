package com.example.book2onandonuserservice.global.exception;

// 1. 최상위 암호화 예외 (Runtime Exception 상속)
public class CryptoException extends RuntimeException {
    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}