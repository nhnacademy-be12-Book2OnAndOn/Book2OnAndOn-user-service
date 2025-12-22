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
public class PointHistoryAdminAdjustRequestDto {
    @NotNull
    private Long userId;
    @NotNull
    private Integer amount;
    private String memo; // 보상 사유
}
