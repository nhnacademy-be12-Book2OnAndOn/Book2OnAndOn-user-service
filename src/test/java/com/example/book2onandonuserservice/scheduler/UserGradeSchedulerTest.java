package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    private Users userA; // 승급 예상
    private Users userB; // 등급 유지

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // DB 기준 정확하게 반영
        basic = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
        royal = new UserGrade(2L, GradeName.ROYAL, 0.02, 100000);
        gold = new UserGrade(3L, GradeName.GOLD, 0.025, 200000);
        platinum = new UserGrade(4L, GradeName.PLATINUM, 0.03, 300000);

        // user mock 준비
        userA = mock(Users.class);
        userB = mock(Users.class);

        when(userA.getUserId()).thenReturn(101L);
        when(userB.getUserId()).thenReturn(202L);

        when(userA.getUserGrade()).thenReturn(basic);
        when(userB.getUserGrade()).thenReturn(royal);
    }

    @Test
    @DisplayName("분기별 등급 산정 - userA PLATINUM 승급 / userB 등급 유지")
    void calculateQuarterlyGrades_success() {

        // cutline 내림차순 기반 정책 제공
        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        // ACTIVE 사용자 두 명
        when(usersRepository.findAllByStatus(Status.ACTIVE))
                .thenReturn(List.of(userA, userB));

        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenReturn(350000L);

        when(orderServiceClient.getNetOrderAmount(eq(202L), any(), any()))
                .thenReturn(120000L);

        scheduler.calculateQuarterlyGrades();

        verify(userA).changeGrade(platinum);

        verify(userGradeHistoryRepository).save(any(UserGradeHistory.class));

        verify(userB, never()).changeGrade(any());
    }

    @Test
    @DisplayName("주문 서비스 오류 발생 시 0원 처리 → 등급 하락 없음")
    void calculateQuarterlyGrades_orderServiceError() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findAllByStatus(Status.ACTIVE))
                .thenReturn(List.of(userA));

        // 주문 API 장애 → 0원 처리
        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenThrow(new RuntimeException("order down"));

        scheduler.calculateQuarterlyGrades();

        verify(userA, never()).changeGrade(any());
    }
}