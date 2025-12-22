package com.example.book2onandonuserservice.user.controller;

import com.example.book2onandonuserservice.global.annotation.AuthCheck;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeCreateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserGradeUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserGradeResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.service.UserGradeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserGradeController {
    private final UserGradeService userGradeService;

    //Post 새 등급 생성
    @PostMapping("/admin/grades")
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<UserGradeResponseDto> createGrade(@Valid @RequestBody UserGradeCreateRequestDto request) {
        UserGradeResponseDto response = userGradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //GET 전체 등급 목록 조회
    @GetMapping("/grades")
    public ResponseEntity<List<UserGradeResponseDto>> getAllGrades() {
        List<UserGradeResponseDto> responses = userGradeService.getAllGrades();
        return ResponseEntity.ok(responses);
    }

    //PUT /{gradeId} 등급 정보 수정
    @PutMapping("/admin/grades/{gradeId}")
    @AuthCheck(Role.MEMBER_ADMIN)
    public ResponseEntity<UserGradeResponseDto> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody UserGradeUpdateRequestDto request
    ) {
        UserGradeResponseDto response = userGradeService.updateGrade(gradeId, request);
        return ResponseEntity.ok(response);
    }
}
