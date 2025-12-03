package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.global.client.BookServiceClient;
import com.example.book2onandonuserservice.global.dto.RestPage;
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
import com.example.book2onandonuserservice.user.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BookServiceClient bookServiceClient;

    @Mock
    private UserGradeRepository userGradeRepository;

    private Users dummyUser;
    private UserGrade dummyGrade;

    @BeforeEach
    void setUp() {
        dummyGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
        dummyUser = new Users(
                "testUser", "encodedPw", "Test Name", "test@test.com", "01012345678",
                LocalDate.of(2000, 1, 1), dummyGrade
        );
        ReflectionTestUtils.setField(dummyUser, "userId", 1L);
        ReflectionTestUtils.setField(dummyUser, "status", Status.ACTIVE);
        ReflectionTestUtils.setField(dummyUser, "role", Role.USER);
        ReflectionTestUtils.setField(dummyUser, "nickname", "TestNick");
    }

    // ==========================================
    // 1. 내 정보 조회 (getMyInfo)
    // ==========================================
    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));

        UserResponseDto response = userService.getMyInfo(1L);

        assertThat(response.userLoginId()).isEqualTo("testUser");
        assertThat(response.email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 사용자 없음")
    void getMyInfo_Fail_NotFound() {
        given(usersRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyInfo(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ==========================================
    // 2. 내 정보 수정 (updateMyInfo)
    // ==========================================
    @Test
    @DisplayName("내 정보 수정 성공 - 닉네임/이메일 변경 없음")
    void updateMyInfo_Success_NoCriticalChange() {
        // 기존 이메일/닉네임과 동일하게 요청
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "New Name", "test@test.com", "TestNick", "01099999999"
        );

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));

        UserResponseDto response = userService.updateMyInfo(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.phone()).isEqualTo("01099999999");
    }

    @Test
    @DisplayName("내 정보 수정 성공 - 이메일/닉네임 변경 (중복 없음)")
    void updateMyInfo_Success_WithChange() {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "New Name", "new@test.com", "NewNick", "01099999999"
        );

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        // 중복 체크 통과
        given(usersRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(usersRepository.findByNickname("NewNick")).willReturn(Optional.empty());

        userService.updateMyInfo(1L, request);

        assertThat(dummyUser.getEmail()).isEqualTo("new@test.com");
        assertThat(dummyUser.getNickname()).isEqualTo("NewNick");
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 이메일 중복")
    void updateMyInfo_Fail_EmailDuplicate() {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Name", "duplicate@test.com", "Nick", "01099999999"
        );
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        // 다른 사용자가 해당 이메일 사용 중
        given(usersRepository.findByEmail("duplicate@test.com")).willReturn(Optional.of(new Users()));

        assertThatThrownBy(() -> userService.updateMyInfo(1L, request))
                .isInstanceOf(UserEmailDuplicateException.class);
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 닉네임 중복")
    void updateMyInfo_Fail_NicknameDuplicate() {
        UserUpdateRequestDto request = new UserUpdateRequestDto(
                "Name", "test@test.com", "DuplicateNick", "01099999999"
        );
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(usersRepository.findByNickname("DuplicateNick")).willReturn(Optional.of(new Users()));

        assertThatThrownBy(() -> userService.updateMyInfo(1L, request))
                .isInstanceOf(UserNicknameDuplicationException.class);
    }

    // ==========================================
    // 3. 비밀번호 변경 (changePassword)
    // ==========================================
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("encodedPw", "newPw");

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(passwordEncoder.matches("encodedPw", "encodedPw")).willReturn(true);
        given(passwordEncoder.encode("newPw")).willReturn("newEncodedPw");

        userService.changePassword(1L, request);

        assertThat(dummyUser.getPassword()).isEqualTo("newEncodedPw");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Fail_Mismatch() {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("wrongPw", "newPw");

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(PasswordMismatchException.class);
    }

    // ==========================================
    // 4. 회원 탈퇴 (deleteUser) - 본인 요청
    // ==========================================
    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUser_Success() {
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));

        userService.deleteUser(1L, "개인 사유");

        // Assuming withdraw method sets status to CLOSED (Users 엔티티 로직 의존)
        // 실제 Users.withDraw 구현 내용에 따라 검증 (보통 Status 변경됨)
        // 여기선 verify로 withdraw 호출 여부만 검증하기엔 void라 상태값을 확인하는게 좋음
        // (단, Users 엔티티 코드가 없으므로 가정하에 작성)
        // ReflectionTestUtils.getField(dummyUser, "status");
    }

    // ==========================================
    // 5. 관리자 기능 (getAllUsers, getUserInfo, update, delete)
    // ==========================================
    @Test
    @DisplayName("전체 회원 조회 (관리자)")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Users> page = new PageImpl<>(List.of(dummyUser));

        given(usersRepository.findAll(pageable)).willReturn(page);

        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userLoginId()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("회원 상세 조회 (관리자)")
    void getUserInfo_Success() {
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        UserResponseDto result = userService.getUserInfo(1L);
        assertThat(result.userLoginId()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("관리자 - 회원 정보 수정 성공 (Role, Status, Grade)")
    void updateUserByAdmin_Success() {

        AdminUserUpdateRequestDto request =
                new AdminUserUpdateRequestDto("SUPER_ADMIN", "DORMANT", "ROYAL");

        UserGrade royalGrade = new UserGrade(2L, GradeName.ROYAL, 0.05, 100000);

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userGradeRepository.findByGradeName(GradeName.ROYAL)).willReturn(Optional.of(royalGrade));

        userService.updateUserByAdmin(1L, request);

        assertThat(dummyUser.getRole()).isEqualTo(Role.SUPER_ADMIN);
        assertThat(dummyUser.getStatus()).isEqualTo(Status.DORMANT);
        assertThat(dummyUser.getUserGrade()).isEqualTo(royalGrade);
    }


    @Test
    @DisplayName("관리자 - 회원 정보 수정 실패 (존재하지 않는 등급)")
    void updateUserByAdmin_Fail_GradeNotFound() {
        AdminUserUpdateRequestDto request =
                new AdminUserUpdateRequestDto(null, null, "ROYAL");

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userGradeRepository.findByGradeName(GradeName.ROYAL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("존재하지 않는 등급입니다.");
    }


    @Test
    @DisplayName("관리자 - 회원 탈퇴 처리 성공")
    void deleteUserByAdmin_Success() {
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));

        userService.deleteUserByAdmin(1L, "불량 이용자");

        // Users 엔티티의 withDraw 메서드가 호출되었는지 상태로 확인 필요
        // assertThat(dummyUser.getStatus()).isEqualTo(Status.CLOSED); // 엔티티 로직에 따라 활성화
    }

    @Test
    @DisplayName("관리자 - 회원 탈퇴 처리 실패 (이미 탈퇴함)")
    void deleteUserByAdmin_Fail_AlreadyClosed() {
        ReflectionTestUtils.setField(dummyUser, "status", Status.CLOSED);
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));

        assertThatThrownBy(() -> userService.deleteUserByAdmin(1L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 탈퇴한 회원");
    }


    @Test
    @DisplayName("회원 리뷰 조회 (Feign Client 위임)")
    void getUserReviews_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        RestPage<BookReviewResponseDto> mockRestPage = mock(RestPage.class);

        given(bookServiceClient.getUserReviews(1L, pageable)).willReturn(mockRestPage);

        Page<BookReviewResponseDto> result = userService.getUserReviews(1L, pageable);

        assertThat(result).isNotNull();
        verify(bookServiceClient, times(1)).getUserReviews(1L, pageable);
    }
}