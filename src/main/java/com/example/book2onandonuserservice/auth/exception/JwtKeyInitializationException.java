package com.example.book2onandonuserservice.auth.exception;

public class JwtKeyInitializationException extends RuntimeException {
    public JwtKeyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}