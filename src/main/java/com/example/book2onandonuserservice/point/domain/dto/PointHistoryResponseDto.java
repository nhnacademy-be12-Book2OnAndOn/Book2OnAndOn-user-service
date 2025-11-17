package com.example.book2onandonuserservice.point.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointHistoryResponseDto {

    private Long pointHistoryId;

    private int pointHistoryChange;

    private int totalPoints;

    private String pointHistoryReason;

    private LocalDateTime pointCreatedDate;

    private LocalDateTime pointExpiredDate;

    private Long orderItemId;
    private Long reviewId;
    private Long returnEntity;
}
