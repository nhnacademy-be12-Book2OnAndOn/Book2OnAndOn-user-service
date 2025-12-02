package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeCreateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserGradeResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.exception.GradeNameDuplicateException;
import com.example.book2onandonuserservice.user.exception.GradeNotFoundException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.service.impl.UserGradeServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserGradeServiceImplTest {

    @InjectMocks
    private UserGradeServiceImpl userGradeService;

    @Mock
    private UserGradeRepository userGradeRepository;


    // 1. 전체 등급 조회
    @Test
    @DisplayName("전체 등급 목록 조회 성공")
    void getAllGrades_Success() {
        UserGrade basic = new UserGrade(1L, GradeName.BASIC, 0.0, 0);
        UserGrade royal = new UserGrade(2L, GradeName.ROYAL, 0.05, 200000);

        given(userGradeRepository.findAll()).willReturn(List.of(basic, royal));

        List<UserGradeResponseDto> result = userGradeService.getAllGrades();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).gradeName()).isEqualTo(GradeName.BASIC.name());
        assertThat(result.get(1).gradeName()).isEqualTo(GradeName.ROYAL.name());
    }

    @Test
    @DisplayName("전체 등급 목록 조회 - 데이터가 없을 때 빈 리스트 반환")
    void getAllGrades_Empty() {
        given(userGradeRepository.findAll()).willReturn(List.of());

        List<UserGradeResponseDto> result = userGradeService.getAllGrades();

        assertThat(result).isEmpty();
    }

    // 2. 등급 생성
    @Test
    @DisplayName("등급 생성 성공")
    void createGrade_Success() {
        UserGradeCreateRequestDto request = new UserGradeCreateRequestDto(GradeName.GOLD, 0.03, 100000);

        given(userGradeRepository.existsByGradeName(GradeName.GOLD)).willReturn(false);

        given(userGradeRepository.save(any(UserGrade.class))).willAnswer(invocation -> {
            UserGrade saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "gradeId", 1L);
            return saved;
        });

        UserGradeResponseDto response = userGradeService.createGrade(request);

        assertThat(response.gradeName()).isEqualTo(GradeName.GOLD.name());
        assertThat(response.pointAddRate()).isEqualTo(0.03);
        assertThat(response.pointCutline()).isEqualTo(100000);

        verify(userGradeRepository).save(any(UserGrade.class));
    }

    @Test
    @DisplayName("등급 생성 실패 - 이미 존재하는 등급 이름")
    void createGrade_Fail_DuplicateName() {
        UserGradeCreateRequestDto request = new UserGradeCreateRequestDto(GradeName.BASIC, 0.01, 0);

        given(userGradeRepository.existsByGradeName(GradeName.BASIC)).willReturn(true);

        assertThatThrownBy(() -> userGradeService.createGrade(request))
                .isInstanceOf(GradeNameDuplicateException.class);
    }

    // 3. 등급 수정
    @Test
    @DisplayName("등급 수정 성공")
    void updateGrade_Success() {
        Long gradeId = 1L;
        UserGrade existingGrade = new UserGrade(gradeId, GradeName.BASIC, 0.01, 0);

        UserGradeUpdateRequestDto request = new UserGradeUpdateRequestDto(0.02, 10000);

        given(userGradeRepository.findById(gradeId)).willReturn(Optional.of(existingGrade));

        UserGradeResponseDto response = userGradeService.updateGrade(gradeId, request);

        assertThat(response.pointAddRate()).isEqualTo(0.02);
        assertThat(response.pointCutline()).isEqualTo(10000);
        assertThat(existingGrade.getUserPointAddRate()).isEqualTo(0.02);
        assertThat(existingGrade.getGradeCutline()).isEqualTo(10000);
    }

    @Test
    @DisplayName("등급 수정 실패 - 존재하지 않는 등급 ID")
    void updateGrade_Fail_NotFound() {
        Long gradeId = 999L;
        UserGradeUpdateRequestDto request = new UserGradeUpdateRequestDto(0.05, 50000);

        given(userGradeRepository.findById(gradeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userGradeService.updateGrade(gradeId, request))
                .isInstanceOf(GradeNotFoundException.class);
    }
}