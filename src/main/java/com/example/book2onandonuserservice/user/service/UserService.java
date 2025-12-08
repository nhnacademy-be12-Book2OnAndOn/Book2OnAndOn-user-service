package com.example.book2onandonuserservice.user.service;

import com.example.book2onandonuserservice.global.dto.MyLikedBookResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.request.AdminUserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    //내 정보 조회
    UserResponseDto getMyInfo(Long userId);

    //내 정보 수정
    UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto request);

    //비밀번호 변경
    void changePassword(Long userId, PasswordChangeRequestDto request);

    //회원탈퇴
    void deleteUser(Long userId, String reason);

    //(admin) 전체 회원 목록 조회
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    //(admin) 특정 회원 조회
    UserResponseDto getUserInfo(Long userId);

    //(admin) 회원 정보 강제 수정 (권한, 상태, 등급)
    void updateUserByAdmin(Long userId, AdminUserUpdateRequestDto request);

    //(admin) 회원 강제 탈퇴
    void deleteUserByAdmin(Long userId, String reason);

    //(공개) 회원 리뷰 목록 조회
    Page<BookReviewResponseDto> getUserReviews(Long userId, Pageable pageable);

    // 회원 좋아요 목록 조회
    Page<MyLikedBookResponseDto> getMyLikedBooks(Long userId, Pageable pageable);
}
