package com.example.book2onandonuserservice.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.point.scheduler.PointExpireScheduler;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void expirePointsJob_success() {

        // given
        List<Long> expiredUsers = List.of(1L, 2L, 3L);

        // now 시각과 비교하는데, 테스트에서는 exact match 대신 any(LocalDateTime.class) 가능
        when(pointHistoryRepository.findUserIdsWithExpiredPoints(any(LocalDateTime.class)))
                .thenReturn(expiredUsers);

        // when
        scheduler.expirePointsJob();

        // then: 각 userId가 1번씩 expirePoints 호출됐는지 검증
        verify(pointHistoryService, times(1)).expirePoints(1L);
        verify(pointHistoryService, times(1)).expirePoints(2L);
        verify(pointHistoryService, times(1)).expirePoints(3L);

        // 호출 횟수 총합 검증(정확성 더 강화)
        verify(pointHistoryService, times(expiredUsers.size())).expirePoints(anyLong());

        // Repository 호출도 검증
        verify(pointHistoryRepository, times(1))
                .findUserIdsWithExpiredPoints(any(LocalDateTime.class));
    }
}
