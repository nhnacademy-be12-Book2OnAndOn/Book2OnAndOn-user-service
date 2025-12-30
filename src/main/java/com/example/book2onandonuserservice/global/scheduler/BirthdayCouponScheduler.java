package com.example.book2onandonuserservice.global.scheduler;

import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayCouponScheduler {

    private final UsersRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    // 한 번에 읽어올 데이터 개수 (메모리 보호용)
    private static final int CHUNK_SIZE = 1000;

    @Scheduled(cron = "0 0 0 1 * *")
    @SchedulerLock(
            name = "birthday_user_task",
            lockAtLeastFor = "30s",
            lockAtMostFor = "10m"
    )
    public void sendBirthdayCouponMessage() {
        int currentMonth = LocalDate.now().getMonthValue();
        log.info("{}월 생일 쿠폰 발급 스케줄러 시작", currentMonth);

        int pageNumber = 0;

        while (true) {
            // 페이지 생성 (pageNumber번째 페이지, 1000개씩)
            PageRequest pageRequest = PageRequest.of(pageNumber, CHUNK_SIZE);

            // DB 조회
            // UserRepository에서 생일자 찾는 쿼리 만들어야 함
            // Slice<Long> findIdsByBirthMonth(Pageable pageable) Pageable 매개변수로 받는걸로
            List<Status> targetStatuses = List.of(Status.ACTIVE, Status.DORMANT);

            Slice<Long> idSlice = userRepository.findIdsByBirthMonth(
                    currentMonth,
                    Role.USER,
                    targetStatuses,
                    pageRequest
            );

            // 메시지 전송
            List<Long> ids = idSlice.getContent();
            for (Long userId : ids) {
                rabbitTemplate.convertAndSend(
                        RabbitConfig.USER_EXCHANGE,
                        RabbitConfig.ROUTING_KEY_BIRTHDAY,
                        userId
                );
            }

            log.info("Page {} 처리 완료 ({}건 전송)", pageNumber, ids.size());

            if (!idSlice.hasNext()) {
                break;
            }
            pageNumber++;
        }

        log.info("모든 생일 쿠폰 발급 요청 완료");
    }
}