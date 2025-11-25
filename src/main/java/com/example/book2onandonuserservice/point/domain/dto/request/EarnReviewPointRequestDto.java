package com.example.book2onandonuserservice.point.domain.dto.request;

import com.example.book2onandonuserservice.point.domain.entity.PointReviewType;
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

    @NotNull
    private Long orderItemId;

    @NotNull
    private PointReviewType reviewType;

}
