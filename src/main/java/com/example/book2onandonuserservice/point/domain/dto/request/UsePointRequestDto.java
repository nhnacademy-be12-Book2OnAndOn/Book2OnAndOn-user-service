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
public class UsePointRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long orderItemId;

    @NotNull
    private Integer useAmount; // 사용할 포인트

    @NotNull
    private Integer allowedMaxUseAmount; // 최대 가용 가능 포인트

}
