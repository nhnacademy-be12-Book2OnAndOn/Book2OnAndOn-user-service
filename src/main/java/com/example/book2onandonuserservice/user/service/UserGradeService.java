package com.example.book2onandonuserservice.user.service;

import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeCreateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserGradeResponseDto;
import java.util.List;

public interface UserGradeService {
    //모든 등급 목록 조회 (관리자 페이지나 클라이언트 등급 안내용)
    List<UserGradeResponseDto> getAllGrades();

    //(admin) 새로운 등급 생성
    UserGradeResponseDto createGrade(UserGradeCreateRequestDto request);

    //(admin) 등급 정책 수정
    UserGradeResponseDto updateGrade(Long gradeId, UserGradeUpdateRequestDto request);
}
