package com.example.book2onandonuserservice.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.scheduler.PointExpireScheduler;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointExpireSchedulerTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointHistoryService pointHistoryService;

    @InjectMocks
    private PointExpireScheduler scheduler;

    @Test
    @DisplayName("만료 대상 유저가 존재하면: 각 userId에 대해 expirePoints를 1회씩 호출")
    void expirePointsJob_success() {
        // given
        List<Long> expiredUsers = List.of(1L, 2L, 3L);

        when(pointHistoryRepository.findUserIdsWithExpiredPoints(any(LocalDateTime.class)))
                .thenReturn(expiredUsers);

        // when
        scheduler.expirePointsJob();

        // then
        verify(pointHistoryRepository, times(1))
                .findUserIdsWithExpiredPoints(any(LocalDateTime.class));

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(pointHistoryService, times(expiredUsers.size())).expirePoints(captor.capture());

        assertEquals(expiredUsers, captor.getAllValues());

        verifyNoMoreInteractions(pointHistoryRepository, pointHistoryService);
    }

    @Test
    @DisplayName("만료 대상 유저가 없으면: expirePoints를 호출 x")
    void expirePointsJob_empty_noCalls() {
        // given
        when(pointHistoryRepository.findUserIdsWithExpiredPoints(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        scheduler.expirePointsJob();

        // then
        verify(pointHistoryRepository, times(1))
                .findUserIdsWithExpiredPoints(any(LocalDateTime.class));
        verify(pointHistoryService, never()).expirePoints(anyLong());

        verifyNoMoreInteractions(pointHistoryRepository, pointHistoryService);
    }
}
