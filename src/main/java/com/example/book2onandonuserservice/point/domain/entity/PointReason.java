package com.example.book2onandonuserservice.point.domain.entity;

public enum PointReason {
    SIGNUP, // 회원가입
    REVIEW, // 리뷰 작성 적립
    ORDER, // 도서 결제 적립 (등급 적립)
    USE, // 포인트 사용
    FAILED, // 포인트 사용 실패 (결제 실패)
    REFUND, // 반품으로 인한 환불
    EXPIRE, // 포인트 유효기간 만료
    WITHDRAW, // 회원탈퇴
    ADMIN_ADJUST, // 관리자 수동 지급/차감

    ORDER_RECLAIM // 반품/취소로 구매 적립 회수
}