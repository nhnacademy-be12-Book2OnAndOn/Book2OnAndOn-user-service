package com.example.book2onandonuserservice.user.domain.dto.response;

import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import lombok.Builder;

@Builder
public record UserGradeResponseDto(
        Long gradeId,
        String gradeName,
        Double pointAddRate,
        Integer pointCutline
) {
    public static UserGradeResponseDto fromEntity(UserGrade entity) {
        return UserGradeResponseDto.builder()
                .gradeId(entity.getGradeId())
                .gradeName(entity.getGradeName().name())
                .pointAddRate(entity.getUserPointAddRate())
                .pointCutline(entity.getGradeCutline())
                .build();

    }
}
