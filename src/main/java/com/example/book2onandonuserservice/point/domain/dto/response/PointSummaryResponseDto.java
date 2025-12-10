package com.example.book2onandonuserservice.point.domain.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointSummaryResponseDto {

    private int totalPoint;       // 현재 보유 포인트
    private int earnedThisMonth;  // 이번달 적립
    private int usedThisMonth;    // 이번달 사용
    private int expiringSoon;     // 7일 내 소멸 예정 포인트
    private LocalDateTime from;   // 이번달 시작일
    private LocalDateTime to;     // 기준일
}
