package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.scheduler.DormantUserScheduler;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DormantUserSchedulerTest {
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private DormantUserScheduler scheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("휴면 전환 스케줄러 - ACTIVE 이면서 3개월 이상 로그인 안한 유저를 휴면으로 변경")
    void checkDormantUser_success() {
        // given
        Users user1 = mock(Users.class);
        Users user2 = mock(Users.class);

        List<Users> targetUsers = List.of(user1, user2);

        when(usersRepository.findByStatusAndLastLoginAtBefore(
                eq(Status.ACTIVE),
                any(LocalDateTime.class)
        )).thenReturn(targetUsers);

        // when
        scheduler.checkDormantUser();

        // then
        verify(user1).changeStatus(Status.DORMANT);
        verify(user2).changeStatus(Status.DORMANT);

        verify(usersRepository).findByStatusAndLastLoginAtBefore(
                eq(Status.ACTIVE),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("휴면 전환 스케줄러 - 대상자가 없는 경우 changeStatus 호출되지 않음")
    void checkDormantUser_noTargets() {
        when(usersRepository.findByStatusAndLastLoginAtBefore(any(), any())).thenReturn(List.of());

        scheduler.checkDormantUser();

        verify(usersRepository).findByStatusAndLastLoginAtBefore(any(), any());
    }


}
