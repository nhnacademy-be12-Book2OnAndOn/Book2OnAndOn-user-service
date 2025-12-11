package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserGradeScheduler scheduler;

    private UserGrade basic;
    private UserGrade royal;
    private UserGrade gold;
    private UserGrade platinum;

    private Users userA;
    private Users userB;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        basic = new UserGrade(1L, GradeName.BASIC, 0.01, 0);
        royal = new UserGrade(2L, GradeName.ROYAL, 0.02, 100000);
        gold = new UserGrade(3L, GradeName.GOLD, 0.025, 200000);
        platinum = new UserGrade(4L, GradeName.PLATINUM, 0.03, 300000);

        // [수정] userA 생성 (기본 생성자 + 초기화 메서드)
        Users realUserA = new Users();
        realUserA.initLocalAccount("userA", "pw", "A", "A");
        realUserA.setContactInfo("a@a.com", "0101234", LocalDate.now());
        realUserA.changeGrade(basic);
        userA = spy(realUserA);

        // [수정] userB 생성
        Users realUserB = new Users();
        realUserB.initLocalAccount("userB", "pw", "B", "B");
        realUserB.setContactInfo("b@b.com", "0105678", LocalDate.now());
        realUserB.changeGrade(royal);
        userB = spy(realUserB);

        ReflectionTestUtils.setField(userA, "userId", 101L);
        ReflectionTestUtils.setField(userB, "userId", 202L);
    }

    @Test
    @DisplayName("분기별 등급 산정 - userA PLATINUM 승급 / userB 등급 유지")
    void calculateQuarterlyGrades_success() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findByStatus(eq(Status.ACTIVE), any()))
                .thenReturn(new PageImpl<>(List.of(userA, userB)));

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
    @DisplayName("주문 서비스 오류 발생 시 0원 처리 → 등급 유지")
    void calculateQuarterlyGrades_orderServiceError() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findByStatus(eq(Status.ACTIVE), any()))
                .thenReturn(new PageImpl<>(List.of(userA)));

        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenThrow(new RuntimeException("ORDER DOWN"));

        scheduler.calculateQuarterlyGrades();

        verify(userA, never()).changeGrade(any());
    }

    @Test
    @DisplayName("주문 금액 null → 0원 처리 → 등급 유지")
    void calculateQuarterlyGrades_nullAmount() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        when(usersRepository.findByStatus(eq(Status.ACTIVE), any()))
                .thenReturn(new PageImpl<>(List.of(userA)));

        when(orderServiceClient.getNetOrderAmount(eq(101L), any(), any()))
                .thenReturn(null);

        scheduler.calculateQuarterlyGrades();

        verify(userA, never()).changeGrade(any());
    }

    @Test
    @DisplayName("여러 페이지 처리 시 page++ 흐름 커버")
    void calculateQuarterlyGrades_multiPage() {

        when(userGradeRepository.findAllByOrderByGradeCutlineDesc())
                .thenReturn(List.of(platinum, gold, royal, basic));

        // [수정] userC 생성 (기본 생성자 + 초기화 메서드)
        Users realUserC = new Users();
        realUserC.initLocalAccount("userC", "pw", "C", "C");
        realUserC.setContactInfo("c@c.com", "0109999", LocalDate.now());
        realUserC.changeGrade(basic);
        Users userC = spy(realUserC);

        ReflectionTestUtils.setField(userC, "userId", 303L);

        Page<Users> page1 = new PageImpl<>(
                List.of(userA, userB),
                PageRequest.of(0, 1000),
                3000
        );

        Page<Users> page2 = new PageImpl<>(
                List.of(userC),
                PageRequest.of(1, 1000),
                3000
        );

        when(usersRepository.findByStatus(eq(Status.ACTIVE), any()))
                .thenReturn(page1)
                .thenReturn(page2)
                .thenReturn(Page.empty());

        when(orderServiceClient.getNetOrderAmount(any(), any(), any()))
                .thenReturn(0L);

        scheduler.calculateQuarterlyGrades();

        verify(usersRepository, times(3)).findByStatus(eq(Status.ACTIVE), any());
        verify(entityManager, atLeastOnce()).flush();
        verify(entityManager, atLeastOnce()).clear();
    }
}