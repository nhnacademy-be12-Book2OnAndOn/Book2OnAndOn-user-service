package com.example.book2onandonuserservice.address.exception;

public class AddressNameDuplicateException extends RuntimeException {
    public AddressNameDuplicateException() {
        super("이미 사용중인 주소 이름입니다.");
    }
}
