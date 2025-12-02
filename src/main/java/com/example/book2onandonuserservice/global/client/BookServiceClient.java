package com.example.book2onandonuserservice.global.client;

import com.example.book2onandonuserservice.global.dto.RestPage;
import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "book-service")
public interface BookServiceClient {
    @GetMapping("/users/{userId}/reviews")
    RestPage<BookReviewResponseDto> getUserReviews(
            @PathVariable("userId") Long userId,
            Pageable pageable
    );

    @DeleteMapping("/books/reviews/{reviewId}")
    void deleteReview(@PathVariable("reviewId") Long reviewId,
                      @RequestHeader("X-User-Id") Long userId);
}
