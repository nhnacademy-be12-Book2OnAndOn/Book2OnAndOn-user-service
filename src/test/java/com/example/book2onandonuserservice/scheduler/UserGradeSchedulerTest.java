package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.client.OrderServiceClient;
import com.example.book2onandonuserservice.global.scheduler.UserGradeScheduler;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.UserGradeHistory;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UserGradeHistoryRepository;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class UserGradeSchedulerTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserGradeRepository userGradeRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private UserGradeScheduler scheduler;

    private UserGrade basic;
    private UserGrade royal;
    private UserGrade gold;
    private UserGrade platinum;

    private Users userA; // 승급 대상
    private Users userB; // 등급 유지 대상

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 정확한 UserGrade 생성자 형태 맞춤
        basic = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
        royal = new UserGrade(2L, GradeName.ROYAL, 0.02, 100000);
        gold = new UserGrade(3L, GradeName.GOLD, 0.025, 200000);
        platinum = new UserGrade(4L, GradeName.PLATINUM, 0.03, 300000);

        // Spy 객체 생성 (mock 아님)
        userA = spy(new Users(
                "userA", "pw", "A", "a@a.com", "0101234", LocalDate.now(), basic
        ));
        userB = spy(new Users(
                "userB", "pw", "B", "b@b.com", "0105678", LocalDate.now(), royal
        ));

        ReflectionTestUtils.setField(userA, "userId", 101L);
        ReflectionTestUtils.setField(userB, "userId", 202L);
    }

    @Test
    @DisplayName("분기별 등급 산정 - userA PLATINUM 승급 / userB 등급 유지")
    void calculateQuarterlyGrades_success() {

        // 정책 내림차순
        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        // ACTIVE 유저 목록
        when(usersRepository.findAllByStatus(Status.ACTIVE))
                .thenReturn(List.of(userA, userB));

        // userA 승급 조건: 35만 원 → PLATINUM
        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenReturn(350000L);

        // userB 유지 조건: 12만 원 → ROYAL 유지
        when(orderServiceClient.getNetOrderAmount(eq(202L), any(), any()))
                .thenReturn(120000L);

        scheduler.calculateQuarterlyGrades();

        // userA는 등급 변경 발생
        verify(userA).changeGrade(platinum);
        verify(userGradeHistoryRepository).save(any(UserGradeHistory.class));

        // userB는 현재 ROYAL → ROYAL 동일 → 변화 없음
        verify(userB, never()).changeGrade(any());
    }

    @Test
    @DisplayName("주문 서비스 오류 발생 시 0원 처리 → 등급 유지")
    void calculateQuarterlyGrades_orderServiceError() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findAllByStatus(Status.ACTIVE))
                .thenReturn(List.of(userA));

        // 주문 API 장애
        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenThrow(new RuntimeException("ORDER DOWN"));

        scheduler.calculateQuarterlyGrades();

        // userA는 BASIC이므로 등급 변화 없음
        verify(userA, never()).changeGrade(any());
    }

    @Test
    @DisplayName("주문 금액 null → 0원 처리 → 등급 유지")
    void calculateQuarterlyGrades_nullAmount() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findAllByStatus(Status.ACTIVE))
                .thenReturn(List.of(userA));

        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenReturn(null); // 핵심

        scheduler.calculateQuarterlyGrades();

        verify(userA, never()).changeGrade(any());
    }
}
