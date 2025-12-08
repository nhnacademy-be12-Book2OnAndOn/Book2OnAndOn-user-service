package com.example.book2onandonuserservice.global.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyLikedBookResponseDto {
    private Long bookLikeId;
    private LocalDateTime createdAt;
    private BookListResponseDto bookInfo; // 위에서 만든 BookListResponseDto 사용
}
