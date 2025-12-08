package com.example.book2onandonuserservice.user.service.impl;

import com.example.book2onandonuserservice.global.client.BookServiceClient;
import com.example.book2onandonuserservice.global.dto.MyLikedBookResponseDto;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookServiceClient bookServiceClient;
    private final UserGradeRepository userGradeRepository;
    private final PointHistoryService pointHistoryService;
    private final RedisUtil redisUtil;

    private Users findUserOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Long fetchUserPoint(Long userId) {
        try {
            CurrentPointResponseDto response = pointHistoryService.getMyCurrentPoint(userId);
            return (long) response.getCurrentPoint();
        } catch (Exception e) {
            log.warn("포인트 조회 실패 (userId={}): {}", userId, e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        Long point = fetchUserPoint(userId); // 포인트 조회
        return UserResponseDto.fromEntity(user, point);
    }

    @Override
    @Transactional
    public UserResponseDto updateMyInfo(Long userId, UserUpdateRequestDto request) {
        Users user = findUserOrThrow(userId);

        if (!Objects.equals(user.getEmail(), request.email())) {
            // 1. 중복 검사
            if (usersRepository.findByEmail(request.email()).isPresent()) {
                throw new UserEmailDuplicateException();
            }

            // 2. [추가] 인증 여부 검사 (Redis 확인)
            // 회원가입 때 쓴 키 패턴: "user:email:verified:{email}" -> "true"
            String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(request.email());
            String isVerified = redisUtil.getData(verifiedKey);

            if (!"true".equals(isVerified)) {
                throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
            }

            // 인증 확인 후 Redis 데이터 삭제 (재사용 방지)
            redisUtil.deleteData(verifiedKey);
        }

        if (!Objects.equals(user.getNickname(), request.nickname())
                && usersRepository.findByNickname(request.nickname()).isPresent()) {
            throw new UserNicknameDuplicationException();
        }

        user.updateProfile(
                request.name(),
                request.email(),
                request.nickname(),
                request.phone()
        );

        return UserResponseDto.fromEntity(user, fetchUserPoint(userId));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto request) {
        Users user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
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
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(Long userId) {
        Users user = findUserOrThrow(userId);
        Long point = fetchUserPoint(userId); // 상세 조회는 포인트 포함
        return UserResponseDto.fromEntity(user, point);
    }

    @Override
    @Transactional
    public void updateUserByAdmin(Long userId, AdminUserUpdateRequestDto request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.role() != null) {
            user.changeRole(Role.valueOf(request.role()));
        }

        if (request.status() != null) {
            user.changeStatus(Status.valueOf(request.status()));
        }

        if (request.gradeName() != null) {
            UserGrade grade = userGradeRepository.findByGradeName(GradeName.valueOf(request.gradeName()))
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

        user.withDraw("[관리자 처리] " + reason);
    }

    @Override
    public Page<BookReviewResponseDto> getUserReviews(Long userId, Pageable pageable) {
        return bookServiceClient.getUserReviews(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MyLikedBookResponseDto> getMyLikedBooks(Long userId, Pageable pageable) {
        return bookServiceClient.getMyLikedBooks(userId, pageable);
    }


}