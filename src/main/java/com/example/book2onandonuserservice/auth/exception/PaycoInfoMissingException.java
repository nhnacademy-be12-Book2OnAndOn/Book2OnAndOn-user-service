package com.example.book2onandonuserservice.auth.exception;

public class PaycoInfoMissingException extends RuntimeException {
    public PaycoInfoMissingException() {
        super("PAYCO_INFO_MISSING");
    }
}