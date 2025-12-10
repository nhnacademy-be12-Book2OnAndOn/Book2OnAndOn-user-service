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
    private Long orderId;

    @NotNull
    private Integer pureAmount;

//    @NotNull
//    private String gradeName;

    @NotNull
    private Double pointAddRate;

}
