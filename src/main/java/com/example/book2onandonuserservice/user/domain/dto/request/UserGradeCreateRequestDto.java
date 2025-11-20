package com.example.book2onandonuserservice.user.domain.dto.request;

import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserGradeCreateRequestDto(
        @NotNull(message = "등급 이름은 필수입니다.")
        GradeName gradeName,
        @NotNull(message = "적립률은 필수입니다.")
        @Min(value = 0, message = "적립률은 0 이상입니다.")
        Double pointAddRate,
        @NotNull(message = "기준 금액은 필수입니다.")
        @Min(value = 0, message = "기준 금액은 0 이상입니다.")
        Integer pointCutline
) {
}
