package com.example.book2onandonuserservice.point.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EarnReviewPointRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long reviewId;

//    @NotNull
//    private Long orderId;
//
//    @NotNull
//    private PointReviewType reviewType; // TEXT / PHOTO

    @NotNull
    private boolean hasImage; // T: review_photo, F: review_text

}
