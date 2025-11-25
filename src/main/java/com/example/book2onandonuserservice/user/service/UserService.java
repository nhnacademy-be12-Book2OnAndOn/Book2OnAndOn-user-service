package com.example.book2onandonuserservice.user.service;

import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;

public interface UserService {
    //내 정보 조회
    UserResponseDto getMyInfo(Long userId);

    //내 정보 수정
    UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto request);

    //비밀번호 변경
    void changePassword(Long userId, PasswordChangeRequestDto request);

    //회원탈퇴
    void deleteUser(Long userId);

    //(admin) 특정 회원 조회
    UserResponseDto getUserInfo(Long userId);
}
