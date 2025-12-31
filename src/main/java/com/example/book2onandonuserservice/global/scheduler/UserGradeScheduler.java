package com.example.book2onandonuserservice.global.scheduler;

import com.example.book2onandonuserservice.global.client.OrderServiceClient;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGradeScheduler {

    private static final int PAGE_SIZE = 1000; // 한 번에 처리할 사용자 수

    private final UsersRepository usersRepository;
    private final UserGradeRepository userGradeRepository;
    private final UserGradeHistoryRepository userGradeHistoryRepository;
    private final OrderServiceClient orderServiceClient;
    private final EntityManager entityManager;

    @Scheduled(cron = "0 * * * * *") // 1·4·7·10월 1일 새벽 4시
    @SchedulerLock(name = "user_grade_task", lockAtLeastFor = "30s", lockAtMostFor = "10m")
    @Transactional
    public void calculateQuarterlyGrades() {
        log.info("분기별 회원 등급 산정 시작");
        long startTime = System.currentTimeMillis();

        LocalDate now = LocalDate.now();
        LocalDate fromDate = now.minusMonths(3).withDayOfMonth(1);
        log.info("집계 대상 기간: {} ~ {}", fromDate, now);

        List<UserGrade> gradePolicies = userGradeRepository.findAllByOrderByGradeCutlineDesc();

        int updatedCount = 0;
        int page = 0;

        Page<Users> activeUsersPage;

        do {
            activeUsersPage = usersRepository.findByStatus(
                    Status.ACTIVE,
                    PageRequest.of(page, PAGE_SIZE)
            );

            if (activeUsersPage.isEmpty()) {
                break;
            }

            log.info("등급 산정 처리 페이지: {}, 사용자 수: {}", page, activeUsersPage.getNumberOfElements());

            for (Users user : activeUsersPage.getContent()) {
                updatedCount += processUserGradeUpdate(user, gradePolicies, fromDate, now);
            }

            entityManager.flush();
            entityManager.clear();

            page++;

        } while (activeUsersPage.hasNext());

        long endTime = System.currentTimeMillis();
        log.info("등급 산정 종료 (업데이트: {}명, 소요시간: {}ms)", updatedCount, (endTime - startTime));
    }

    /**
     * 개별 회원 등급 산정 처리
     */
    private int processUserGradeUpdate(
            Users user,
            List<UserGrade> gradePolicies,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        try {
            long netAmount = fetchNetOrderAmount(user.getUserId(), fromDate, toDate);
            return applyGradePolicy(user, gradePolicies, netAmount, fromDate, toDate);

        } catch (Exception e) {
            log.error("회원(ID: {}) 등급 산정 중 오류 발생", user.getUserId(), e);
            return 0;
        }
    }

    /**
     * 주문 서비스에서 순수 주문 금액 조회 (실패 시 0원 처리)
     */
    private long fetchNetOrderAmount(Long userId, LocalDate from, LocalDate to) {
        try {
            Long amount = orderServiceClient.getNetOrderAmount(userId, from, to);
            return (amount == null) ? 0L : amount;

        } catch (Exception e) {
            log.warn("주문 서비스 통신 실패 (User ID: {}) - 0원으로 처리", userId);
            return 0L;
        }
    }

    /**
     * 등급 정책을 적용하여 적절한 등급으로 설정
     */
    private int applyGradePolicy(
            Users user,
            List<UserGrade> gradePolicies,
            long netAmount,
            LocalDate from,
            LocalDate to
    ) {
        for (UserGrade policy : gradePolicies) {
            if (netAmount >= policy.getGradeCutline()) {
                return updateUserGradeIfNeeded(user, policy, from, to);
            }
        }
        return 0;
    }

    /**
     * 등급이 변경될 필요가 있을 경우 업데이트 + 변경 이력 추가
     */
    private int updateUserGradeIfNeeded(
            Users user,
            UserGrade newGrade,
            LocalDate from,
            LocalDate to
    ) {
        if (user.getUserGrade() != null
                && user.getUserGrade().getGradeName() == newGrade.getGradeName()) {
            return 0;
        }

        String prev = (user.getUserGrade() == null)
                ? "NONE"
                : user.getUserGrade().getGradeName().name();

        user.changeGrade(newGrade);

        UserGradeHistory history = new UserGradeHistory(
                user,
                prev,
                newGrade.getGradeName().name(),
                "정기 등급 산정 (" + from + "~" + to + ")"
        );

        userGradeHistoryRepository.save(history);

        log.info("User {} 등급 변경: {} -> {}", user.getUserId(), prev, newGrade.getGradeName());
        return 1;
    }
}
