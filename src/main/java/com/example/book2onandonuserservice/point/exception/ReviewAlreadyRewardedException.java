package com.example.book2onandonuserservice.point.exception;

public class ReviewAlreadyRewardedException extends RuntimeException {
    public ReviewAlreadyRewardedException(Long reviewId) {
        super("이미 적립된 리뷰입니다. reviewId = " + reviewId);
    }
}
