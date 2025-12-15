package com.example.book2onandonuserservice.global.scheduler;

import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDateTime;
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
public class DormantUserScheduler {
    private final UsersRepository usersRepository;

    //매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "dormant_user_task",
            lockAtLeastFor = "30s",
            lockAtMostFor = "10m")
    @Transactional
    public void checkDormantUser() {
        log.info("휴면 회원 전환 스케줄러 시작");

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<Users> targets = usersRepository.findByStatusAndLastLoginAtBefore(Status.ACTIVE, threeMonthsAgo);

        int count = 0;
        for (Users user : targets) {
            user.changeStatus(Status.DORMANT);
            count++;
        }

        log.info("휴면 회원 전환 완료: 총 {}명", count);


    }
}
