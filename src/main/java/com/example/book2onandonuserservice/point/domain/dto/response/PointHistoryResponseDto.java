package com.example.book2onandonuserservice.point.domain.dto.response;

import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointHistoryResponseDto {

    private Long pointHistoryId;

    private int pointHistoryChange;

    private int totalPoints;

    private LocalDateTime pointCreatedDate;

    private LocalDateTime pointExpiredDate;

    private Integer remainingPoint;

    private PointReason pointReason;

    private Long orderId;

    private Long reviewId;

    private Long returnId;

}
