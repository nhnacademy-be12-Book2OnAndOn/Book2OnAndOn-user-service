package com.example.book2onandonuserservice.user.domain.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookReviewResponseDto {
    private Long id;    // 리뷰아이디
    private Long bookId;// 도서아이디
    private Long userId;// 회원아이디
    private String title;// 리뷰 제목
    private String content;// 리뷰 내용
    private Integer score;// 평가점수
    private LocalDate createdAt;//생성 일시
    private List<ReviewImageResponse> images; // 리뷰 이미지 리스트

    @Getter
    @NoArgsConstructor
    public static class ReviewImageResponse {
        private Long id;
        private String imagePath;
    }
}
