package com.example.book2onandonuserservice.global.scheduler;

import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionMigrationScheduler {

    private final UsersRepository usersRepository;
    private final TransactionTemplate transactionTemplate; // 트랜잭션 수동 제어를 위해 주입
    private static final int BATCH_SIZE = 100;

    @Scheduled(cron = "0 45 14 * * *")
    @SchedulerLock(name = "encryption_migration_task", lockAtLeastFor = "30s", lockAtMostFor = "10m")
    public void runMigrationTask() {
        log.info("[Encryption Migration] 배치 작업을 시작합니다.");

        long totalCount = usersRepository.count();
        if (totalCount == 0) {
            log.info("[Encryption Migration] 마이그레이션 대상 유저가 없습니다.");
            return;
        }

        int totalPages = (int) Math.ceil((double) totalCount / BATCH_SIZE);
        log.info("[Encryption Migration] 총 {} 명, {} 페이지 처리 예정", totalCount, totalPages);

        for (int i = 0; i < totalPages; i++) {
            int pageNumber = i;

            transactionTemplate.executeWithoutResult(status -> {
                try {
                    processBatch(pageNumber);
                } catch (Exception e) {
                    log.error("[Encryption Migration] 배치 {}/{} 처리 중 오류 발생", pageNumber + 1, totalPages, e);
                    status.setRollbackOnly(); // 해당 배치만 롤백
                }
            });

            log.info("[Encryption Migration] 배치 {}/{} 완료", i + 1, totalPages);
        }

        log.info("[Encryption Migration] 배치 작업 종료.");
    }

    private void processBatch(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
        Page<Users> usersPage = usersRepository.findAll(pageable);

        for (Users user : usersPage) {
            usersRepository.save(user);
        }
    }
}