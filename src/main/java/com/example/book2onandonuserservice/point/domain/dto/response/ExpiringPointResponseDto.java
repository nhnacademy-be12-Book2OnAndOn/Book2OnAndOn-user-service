package com.example.book2onandonuserservice.point.domain.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// 7일 내 소멸 예정 포인트 조회
public class ExpiringPointResponseDto {
    private int expiringAmount;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
