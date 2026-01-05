package com.example.book2onandonuserservice.point.domain.dto.request.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarnReviewPointInternalRequestDto {
    private Long userId;
    private Long reviewId;
    private boolean hasImage;
}
