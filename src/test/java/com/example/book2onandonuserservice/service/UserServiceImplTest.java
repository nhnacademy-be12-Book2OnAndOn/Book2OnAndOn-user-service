//package com.example.book2onandonuserservice.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import com.example.book2onandonuserservice.global.client.BookServiceClient;
//import com.example.book2onandonuserservice.global.dto.RestPage;
//import com.example.book2onandonuserservice.global.util.RedisUtil;
//import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
//import com.example.book2onandonuserservice.point.service.PointHistoryService;
//import com.example.book2onandonuserservice.user.domain.dto.request.AdminUserUpdateRequestDto;
//import com.example.book2onandonuserservice.user.domain.dto.request.PasswordChangeRequestDto;
//import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
//import com.example.book2onandonuserservice.user.domain.dto.response.BookReviewResponseDto;
//import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
//import com.example.book2onandonuserservice.user.domain.entity.GradeName;
//import com.example.book2onandonuserservice.user.domain.entity.Role;
//import com.example.book2onandonuserservice.user.domain.entity.Status;
//import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
//import com.example.book2onandonuserservice.user.domain.entity.Users;
//import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
//import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
//import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
//import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
//import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
//import com.example.book2onandonuserservice.user.repository.UsersRepository;
//import com.example.book2onandonuserservice.user.service.impl.UserServiceImpl;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceImplTest {
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    @Mock
//    private UsersRepository usersRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private BookServiceClient bookServiceClient;
//
//    @Mock
//    private UserGradeRepository userGradeRepository;
//
//    @Mock
//    private PointHistoryService pointHistoryService;
//
//    @Mock
//    private RedisUtil redisUtil;
//
//
//    private Users dummyUser;
//    private UserGrade dummyGrade;
//
//    @BeforeEach
//    void setUp() {
//        dummyGrade = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
//        dummyUser = new Users(
//                "testUser", "encodedPw", "Test Name", "test@test.com",
//                "01012345678", LocalDate.of(2000, 1, 1), dummyGrade
//        );
//
//        ReflectionTestUtils.setField(dummyUser, "userId", 1L);
//        ReflectionTestUtils.setField(dummyUser, "status", Status.ACTIVE);
//        ReflectionTestUtils.setField(dummyUser, "role", Role.USER);
//        ReflectionTestUtils.setField(dummyUser, "nickname", "TestNick");
//    }
//
//    // 내 정보 조회
//    @Test
//    @DisplayName("내 정보 조회 성공")
//    void getMyInfo_Success() {
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(pointHistoryService.getMyCurrentPoint(1L))
//                .willReturn(new CurrentPointResponseDto(0));
//
//        UserResponseDto response = userService.getMyInfo(1L);
//
//        assertThat(response.userLoginId()).isEqualTo("testUser");
//        assertThat(response.email()).isEqualTo("test@test.com");
//    }
//
//    @Test
//    @DisplayName("내 정보 조회 실패 - 사용자 없음")
//    void getMyInfo_Fail_NotFound() {
//        given(usersRepository.findById(99L)).willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.getMyInfo(99L))
//                .isInstanceOf(UserNotFoundException.class);
//    }
//
//    // 내 정보 수정
//    @Test
//    @DisplayName("내 정보 수정 성공 - 주요 정보 변경 없음")
//    void updateMyInfo_Success_NoCriticalChange() {
//        UserUpdateRequestDto request = new UserUpdateRequestDto(
//                "New Name", "test@test.com", "TestNick", "01099999999"
//        );
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(pointHistoryService.getMyCurrentPoint(1L))
//                .willReturn(new CurrentPointResponseDto(0));
//
//        UserResponseDto response = userService.updateMyInfo(1L, request);
//
//        assertThat(response.name()).isEqualTo("New Name");
//        assertThat(response.phone()).isEqualTo("01099999999");
//    }
//
//    @Test
//    @DisplayName("내 정보 수정 성공 - 이메일, 닉네임 변경")
//    void updateMyInfo_Success_WithChange() {
//        UserUpdateRequestDto request = new UserUpdateRequestDto(
//                "New Name", "new@test.com", "NewNick", "01099999999"
//        );
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(usersRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
//        given(usersRepository.findByNickname("NewNick")).willReturn(Optional.empty());
//        given(pointHistoryService.getMyCurrentPoint(1L))
//                .willReturn(new CurrentPointResponseDto(0));
//
//        given(redisUtil.getData(anyString())).willReturn("true");
//
//        doNothing().when(redisUtil).deleteData(anyString());
//
//        userService.updateMyInfo(1L, request);
//
//        assertThat(dummyUser.getEmail()).isEqualTo("new@test.com");
//        assertThat(dummyUser.getNickname()).isEqualTo("NewNick");
//    }
//
//
//    @Test
//    @DisplayName("내 정보 수정 실패 - 이메일 중복")
//    void updateMyInfo_Fail_EmailDuplicate() {
//        UserUpdateRequestDto request = new UserUpdateRequestDto(
//                "Name", "duplicate@test.com", "Nick", "01099999999"
//        );
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(usersRepository.findByEmail("duplicate@test.com"))
//                .willReturn(Optional.of(new Users()));
//
//        assertThatThrownBy(() -> userService.updateMyInfo(1L, request))
//                .isInstanceOf(UserEmailDuplicateException.class);
//    }
//
//    @Test
//    @DisplayName("내 정보 수정 실패 - 닉네임 중복")
//    void updateMyInfo_Fail_NicknameDuplicate() {
//        UserUpdateRequestDto request = new UserUpdateRequestDto(
//                "Name", "test@test.com", "DuplicateNick", "01099999999"
//        );
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(usersRepository.findByNickname("DuplicateNick"))
//                .willReturn(Optional.of(new Users()));
//
//        assertThatThrownBy(() -> userService.updateMyInfo(1L, request))
//                .isInstanceOf(UserNicknameDuplicationException.class);
//    }
//
//    // ==========================================
//    // 3. 비밀번호 변경 (changePassword)
//    // ==========================================
//    @Test
//    @DisplayName("비밀번호 변경 성공")
//    void changePassword_Success() {
//        PasswordChangeRequestDto request = new PasswordChangeRequestDto("encodedPw", "newPw");
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(passwordEncoder.matches("encodedPw", "encodedPw")).willReturn(true);
//        given(passwordEncoder.encode("newPw")).willReturn("encodedNewPw");
//
//        userService.changePassword(1L, request);
//
//        assertThat(dummyUser.getPassword()).isEqualTo("encodedNewPw");
//    }
//
//    @Test
//    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 틀림")
//    void changePassword_Fail_Mismatch() {
//        PasswordChangeRequestDto request = new PasswordChangeRequestDto("wrongPw", "newPw");
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);
//
//        assertThatThrownBy(() -> userService.changePassword(1L, request))
//                .isInstanceOf(PasswordMismatchException.class);
//    }
//
//    // 탈퇴
//    @Test
//    @DisplayName("회원 탈퇴 성공")
//    void deleteUser_Success() {
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//
//        userService.deleteUser(1L, "사유");
//
//        assertThat(dummyUser.getStatus()).isEqualTo(Status.CLOSED);
//    }
//
//    // 관리자 기능
//    @Test
//    @DisplayName("관리자 - 전체 회원 조회")
//    void getAllUsers_Success() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Users> page = new PageImpl<>(List.of(dummyUser));
//
//        given(usersRepository.findAll(pageable)).willReturn(page);
//
//        Page<UserResponseDto> result = userService.getAllUsers(pageable);
//
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).userLoginId()).isEqualTo("testUser");
//    }
//
//    @Test
//    @DisplayName("관리자 - 회원 정보 조회")
//    void getUserInfo_Success() {
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(pointHistoryService.getMyCurrentPoint(1L))
//                .willReturn(new CurrentPointResponseDto(0));
//
//        UserResponseDto result = userService.getUserInfo(1L);
//
//        assertThat(result.userLoginId()).isEqualTo("testUser");
//    }
//
//    @Test
//    @DisplayName("관리자 - 유저 정보 수정 성공")
//    void updateUserByAdmin_Success() {
//        AdminUserUpdateRequestDto request =
//                new AdminUserUpdateRequestDto("SUPER_ADMIN", "DORMANT", "ROYAL");
//
//        UserGrade royal = new UserGrade(2L, GradeName.ROYAL, 0.05, 100000);
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(userGradeRepository.findByGradeName(GradeName.ROYAL)).willReturn(Optional.of(royal));
//
//        userService.updateUserByAdmin(1L, request);
//
//        assertThat(dummyUser.getRole()).isEqualTo(Role.SUPER_ADMIN);
//        assertThat(dummyUser.getStatus()).isEqualTo(Status.DORMANT);
//        assertThat(dummyUser.getUserGrade()).isEqualTo(royal);
//    }
//
//    @Test
//    @DisplayName("관리자 - 등급 변경 실패 (없는 등급)")
//    void updateUserByAdmin_Fail_GradeNotFound() {
//        AdminUserUpdateRequestDto request =
//                new AdminUserUpdateRequestDto(null, null, "ROYAL");
//
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//        given(userGradeRepository.findByGradeName(GradeName.ROYAL))
//                .willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.updateUserByAdmin(1L, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("존재하지 않는 등급입니다.");
//    }
//
//    @Test
//    @DisplayName("관리자 - 회원 탈퇴 실패 (이미 탈퇴)")
//    void deleteUserByAdmin_Fail_AlreadyClosed() {
//        ReflectionTestUtils.setField(dummyUser, "status", Status.CLOSED);
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//
//        assertThatThrownBy(() -> userService.deleteUserByAdmin(1L, "reason"))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("이미 탈퇴한 회원");
//    }
//
//    @Test
//    @DisplayName("관리자 - 회원 탈퇴 성공")
//    void deleteUserByAdmin_Success() {
//        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
//
//        userService.deleteUserByAdmin(1L, "관리자 사유");
//
//        assertThat(dummyUser.getStatus()).isEqualTo(Status.CLOSED);
//    }
//
//    // 유저 리뷰 조회
//    @Test
//    @DisplayName("회원 리뷰 조회 (Feign)")
//    void getUserReviews_Success() {
//        Pageable pageable = PageRequest.of(0, 10);
//
//        RestPage<BookReviewResponseDto> mockPage = mock(RestPage.class);
//        given(bookServiceClient.getUserReviews(1L, pageable)).willReturn(mockPage);
//
//        Page<BookReviewResponseDto> result = userService.getUserReviews(1L, pageable);
//
//        assertThat(result).isNotNull();
//        verify(bookServiceClient, times(1)).getUserReviews(1L, pageable);
//    }
//}
