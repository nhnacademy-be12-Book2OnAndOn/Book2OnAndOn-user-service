package com.example.book2onandonuserservice.user.service.impl;

import com.example.book2onandonuserservice.global.client.BookServiceClient;
import com.example.book2onandonuserservice.user.domain.dto.request.AdminUserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import com.example.book2onandonuserservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookServiceClient bookServiceClient;
    private final UserGradeRepository userGradeRepository;

    private Users findUserOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        return UserResponseDto.fromEntity(user, 0L);
    }

    @Override
    @Transactional
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
        return UserResponseDto.fromEntity(user, 0L);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto request) {
        Users user = findUserOrThrow(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.changePassword(encodedPassword);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String reason) {
        Users user = findUserOrThrow(userId);
        user.withDraw(reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return usersRepository.findAll(pageable)
                .map(user -> UserResponseDto.fromEntity(user, 0L));
    }

    @Override
    @Transactional
    public UserResponseDto getUserInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        return UserResponseDto.fromEntity(user, 0L);
    }

    @Override
    @Transactional
    public void updateUserByAdmin(Long userId, AdminUserUpdateRequestDto request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getRole() != null) {
            user.changeRole(Role.valueOf(request.getRole()));
        }

        if (request.getStatus() != null) {
            user.changeStatus(Status.valueOf(request.getStatus())); // Users 엔티티에 메서드 필요
        }

        if (request.getGradeName() != null) {
            UserGrade grade = userGradeRepository.findByGradeName(GradeName.valueOf(request.getGradeName()))
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 등급입니다."));
            user.changeGrade(grade);

        }
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(Long userId, String reason) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() == Status.CLOSED) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }

        user.withDraw("[관리자 처리]" + reason);
    }

    @Override
    public Page<BookReviewResponseDto> getUserReviews(Long userId, Pageable pageable) {
        return bookServiceClient.getUserReviews(userId, pageable);
    }
}
