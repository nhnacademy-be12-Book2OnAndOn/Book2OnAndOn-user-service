package com.example.book2onandonuserservice.point.scheduler;

import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointExpireScheduler {

    private final PointHistoryRepository pointHistoryRepository;
    private final PointHistoryService pointHistoryService;

    // 매일 새벽 0시에 만료 포인트 처리
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "pointExpireJob")
    public void expirePointsJob() {
        LocalDateTime now = LocalDate.now().atStartOfDay();
        List<Long> userIds = pointHistoryRepository.findUserIdsWithExpiredPoints(now);

        int count = 0;
        for (Long userId : userIds) {
            log.info("포인트 만료처리 스케줄러 시작: {}", LocalDateTime.now());
            pointHistoryService.expirePoints(userId);
            count++;
        }
        log.info("{}개의 포인트 만료처리 완료", count);
    }
}
