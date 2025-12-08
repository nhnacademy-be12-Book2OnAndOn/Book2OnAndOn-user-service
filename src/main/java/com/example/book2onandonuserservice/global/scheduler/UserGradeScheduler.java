package com.example.book2onandonuserservice.global.scheduler;

import com.example.book2onandonuserservice.global.client.OrderServiceClient;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.UserGradeHistory;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UserGradeHistoryRepository;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGradeScheduler {
    private final UsersRepository usersRepository;
    private final UserGradeRepository userGradeRepository;
    private final UserGradeHistoryRepository userGradeHistoryRepository;
    private final OrderServiceClient orderServiceClient;

    @Scheduled(cron = "0 0 4 1 1,4,7,10 *") //1,4,7,10월 1일 4시정각
    @SchedulerLock(
            name = "user_grade_task",
            lockAtLeastFor = "30s",
            lockAtMostFor = "10m"
    )
    @Transactional
    public void calculateQuarterlyGrades() {
        log.info("분기별 회원 등급 산정 시작");
        long startTime = System.currentTimeMillis();

        //집계 기간 계산
        LocalDate now = LocalDate.now();
        LocalDate fromDate = now.minusMonths(3).withDayOfMonth(1);
        LocalDate toDate = now.minusDays(1);

        log.info("집계 대상 기간: {} ~ {}", fromDate, toDate);

        //등급정책 로딩( Platinum 부터 내림차순)
        List<UserGrade> gradePolicies = userGradeRepository.findAllByOrderByGradeCutlineDesc();

        //대상회원 조회 (휴면, 탈퇴회원 제외)
        List<Users> activeUsers = usersRepository.findAllByStatus(Status.ACTIVE);

        int updateCount = 0;
        int failCount = 0;

        //해당기간 순수 주문금액 조회
        //조회 실패시, 건너뛰고 로그를 남김
        for (Users user : activeUsers) {
            try {
                Long netAmount = 0L;
                // OrderService 연결시
                try {
                    netAmount = orderServiceClient.getNetOrderAmount(user.getUserId(), fromDate, toDate);
                    if (netAmount == null) {
                        netAmount = 0L;
                    }
                } catch (Exception e) {
                    log.warn("주문 서비스 통신 실패 (User ID: {}) - 0원으로 처리", user.getUserId());
                    netAmount = 0L;
                }

                // 등급 판별 로직
                for (UserGrade policy : gradePolicies) {
                    if (netAmount >= policy.getGradeCutline()) {

                        // 현재 등급과 다를 경우에만 업데이트
                        if (user.getUserGrade().getGradeName() != policy.getGradeName()) {

                            String prevGradeName = user.getUserGrade().getGradeName().name();

                            user.changeGrade(policy);

                            UserGradeHistory history = new UserGradeHistory(
                                    user,
                                    prevGradeName,
                                    policy.getGradeName().name(),
                                    "정기 등급 산정 (" + fromDate + "~" + toDate + ")"
                            );
                            userGradeHistoryRepository.save(history);

                            updateCount++;
                            log.info("User {} 등급 변경: {} -> {}", user.getUserId(), prevGradeName, policy.getGradeName());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("회원(ID: {}) 등급 산정 중 오류 발생", user.getUserId(), e);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("등급 산정 종료 (업데이트: {}명, 소요시간: {}ms)",
                updateCount, (endTime - startTime));
    }
}
