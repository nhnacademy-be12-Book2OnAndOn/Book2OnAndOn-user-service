package com.example.book2onandonuserservice.addres.exception;

//주소 10개 초과
public class AddressLimitExceededException extends RuntimeException {
    public AddressLimitExceededException() {
        super("주소는 최대 10개까지 등록할 수 있습니다.");
    }
}
