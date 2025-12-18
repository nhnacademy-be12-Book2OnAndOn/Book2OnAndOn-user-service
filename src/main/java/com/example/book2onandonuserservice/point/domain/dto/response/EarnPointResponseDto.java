package com.example.book2onandonuserservice.point.domain.dto.response;

import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EarnPointResponseDto {
    private int changedPoint;
    private int totalPointAfter;
    private PointReason earnReason;    // SIGNUP / REVIEW / ORDER / REFUND / ADMIN_ADJUST 등
}

