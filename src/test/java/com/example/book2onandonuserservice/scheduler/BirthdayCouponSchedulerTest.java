package com.example.book2onandonuserservice.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.global.scheduler.BirthdayCouponScheduler;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class BirthdayCouponSchedulerTest {

    @Mock
    private UsersRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private BirthdayCouponScheduler birthdayCouponScheduler;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("생일 쿠폰 스케줄러 - 여러 페이지 정상처리 테스트")
    void sendBirthdayCouponMessage_success_multiPage() {
        // given
        int currentMonth = LocalDate.now().getMonthValue();
        List<Status> targetStatuses = List.of(Status.ACTIVE, Status.DORMANT);

        Slice<Long> page1 = new SliceImpl<>(List.of(1L, 2L), PageRequest.of(0, 1000), true);
        Slice<Long> page2 = new SliceImpl<>(List.of(3L), PageRequest.of(1, 1000), false);

        // stubbing: pageable만 달라지므로, 연속 반환으로 처리
        when(userRepository.findIdsByBirthMonth(anyInt(), any(Role.class), anyList(), any(Pageable.class)))
                .thenReturn(page1, page2);

        // when
        birthdayCouponScheduler.sendBirthdayCouponMessage();

        // then: 메시지 3건 전송
        verify(rabbitTemplate, times(3))
                .convertAndSend(eq(RabbitConfig.USER_EXCHANGE), eq(RabbitConfig.ROUTING_KEY_BIRTHDAY), anyLong());

        // then: repository는 2번 조회 + 인자 검증(캡처)
        verify(userRepository, times(2))
                .findIdsByBirthMonth(eq(currentMonth), eq(Role.USER), eq(targetStatuses), pageableCaptor.capture());

        List<Pageable> captured = pageableCaptor.getAllValues();
        assertEquals(2, captured.size());

        // 첫 호출: page=0, size=1000
        assertEquals(0, captured.get(0).getPageNumber());
        assertEquals(1000, captured.get(0).getPageSize());

        // 두 번째 호출: page=1, size=1000
        assertEquals(1, captured.get(1).getPageNumber());
        assertEquals(1000, captured.get(1).getPageSize());
    }
}
