package com.example.book2onandonuserservice.user.service.impl;

import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeCreateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserGradeResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.exception.GradeNameDuplicateException;
import com.example.book2onandonuserservice.user.exception.GradeNotFoundException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.service.UserGradeService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGradeServiceImpl implements UserGradeService {
    private final UserGradeRepository userGradeRepository;

    @Override
    public List<UserGradeResponseDto> getAllGrades() {
        return userGradeRepository.findAll().stream()
                .map(UserGradeResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserGradeResponseDto createGrade(UserGradeCreateRequestDto request) {
        if (userGradeRepository.existsByGradeName(request.gradeName())) {
            throw new GradeNameDuplicateException(request.gradeName().name());
        }
        UserGrade userGrade = new UserGrade(
                request.gradeName(),
                request.pointAddRate(),
                request.pointCutline()
        );
        UserGrade savedGrade = userGradeRepository.save(userGrade);
        return UserGradeResponseDto.fromEntity(savedGrade);
    }

    @Override
    @Transactional
    public UserGradeResponseDto updateGrade(Long gradeId, UserGradeUpdateRequestDto request) {
        UserGrade userGrade = userGradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));

        userGrade.updateGradeInfo(
                request.pointAddRate(),
                request.pointCutline()
        );
        return UserGradeResponseDto.fromEntity(userGrade);
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        UserGrade userGrade = userGradeRepository.findById(gradeId)
                .orElseThrow(() -> new GradeNotFoundException(gradeId));
        userGradeRepository.delete(userGrade);
    }
}
