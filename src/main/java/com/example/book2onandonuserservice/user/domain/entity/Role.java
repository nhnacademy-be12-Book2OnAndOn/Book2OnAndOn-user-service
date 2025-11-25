package com.example.book2onandonuserservice.user.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반회원"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN", "총괄 관리자"),
    BOOK_ADMIN("ROLE_BOOK_ADMIN", "도서 관리자"),
    COUPON_ADMIN("ROLE_COUPON_ADMIN", "쿠폰 관리자"),
    ORDER_ADMIN("ROLE_ORDER_ADMIN", "주문 관리자"),
    MEMBER_ADMIN("ROLE_MEMBER_ADMIN", "회원 관리자");
    private final String key;
    private final String title;

}
