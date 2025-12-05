package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.global.scheduler.BirthdayCouponScheduler;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

class BirthdayCouponSchedulerTest {
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private BirthdayCouponScheduler birthdayCouponScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("생일 쿠폰 스케줄러 - 여러 페이지 정상처리 테스트")
    void sendBirthdayCouponMessage_success() {
        int currentMonth = LocalDate.now().getMonthValue();

        SliceImpl<Long> page1 = new SliceImpl<>(
                List.of(1L, 2L),
                PageRequest.of(0, 1000),
                true
        );

        SliceImpl<Long> page2 = new SliceImpl<>(
                List.of(3L),
                PageRequest.of(1, 1000),
                false
        );

        when(usersRepository.findIdsByBirthMonth(
                eq(currentMonth),
                eq(Role.USER),
                anyList(),
                eq(PageRequest.of(0, 1000))
        )).thenReturn(page1);

        when(usersRepository.findIdsByBirthMonth(
                eq(currentMonth),
                eq(Role.USER),
                anyList(),
                eq(PageRequest.of(1, 1000))
        )).thenReturn(page2);

        birthdayCouponScheduler.sendBirthdayCouponMessage();

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BIRTHDAY, 1L);
        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BIRTHDAY, 2L);
        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BIRTHDAY, 3L);

        verify(rabbitTemplate, times(3))
                .convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.ROUTING_KEY_BIRTHDAY), anyLong());

    }
}
