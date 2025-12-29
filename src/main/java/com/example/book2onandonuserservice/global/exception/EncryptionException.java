package com.example.book2onandonuserservice.global.exception;

public class EncryptionException extends CryptoException {
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}