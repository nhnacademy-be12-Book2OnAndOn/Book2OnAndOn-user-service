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
public class EarnOrderPointRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long orderItemId;

    @NotNull
    private Integer orderAmount; // 결제 금액 * 적립률

}
