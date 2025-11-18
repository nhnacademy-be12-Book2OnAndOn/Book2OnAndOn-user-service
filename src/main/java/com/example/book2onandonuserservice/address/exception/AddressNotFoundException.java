package com.example.book2onandonuserservice.address.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException() {
        super("주소를 찾을 수 없거나 권한이 없습니다.");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}
