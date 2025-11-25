package com.example.book2onandonuserservice.user.service.impl;

import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import com.example.book2onandonuserservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    private Users findUserOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public UserResponseDto getMyInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        return UserResponseDto.fromEntity(user);
    }

    @Override
    public UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto request) {
        Users user = findUserOrThrow(userId);
        if (!user.getEmail().equals(request.email()) && usersRepository.findByEmail(request.email()).isPresent()) {
            throw new UserEmailDuplicateException();
        }

        if (!user.getNickname().equals(request.nickname()) && usersRepository.findByNickname(request.nickname())
                .isPresent()) {
            throw new UserNicknameDuplicationException();
        }
        user.updateProfile(
                request.name(),
                request.email(),
                request.nickname(),
                request.phone()
        );
        return UserResponseDto.fromEntity(user);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequestDto request) {
        Users user = findUserOrThrow(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.changePassword(encodedPassword);
    }

    @Override
    public void deleteUser(Long userId) {
        Users user = findUserOrThrow(userId);
        user.withDraw();
    }

    @Override
    public UserResponseDto getUserInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        return UserResponseDto.fromEntity(user);
    }
}
