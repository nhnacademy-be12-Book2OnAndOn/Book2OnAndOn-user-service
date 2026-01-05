package com.example.book2onandonuserservice.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.book2onandonuserservice.global.event.OrderCanceledEventConsumer;
import com.example.book2onandonuserservice.point.domain.event.OrderCanceledEvent;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCanceledEventConsumerTest {

    @Mock
    private PointHistoryService pointHistoryService;

    @InjectMocks
    private OrderCanceledEventConsumer consumer;

    @Test
    @DisplayName("event가 null이면 서비스 호출 없이 즉시 종료된다")
    void onMessage_eventNull_returnsImmediately() {
        assertDoesNotThrow(() -> consumer.onMessage(null));
        verifyNoInteractions(pointHistoryService);
    }

    @Test
    @DisplayName("userId가 null이면 서비스는 호출하지 않는다")
    void onMessage_userIdNull_returnsWithoutCallingService() {
        OrderCanceledEvent event = new OrderCanceledEvent(
                null,
                10L,
                100,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> consumer.onMessage(event));
        verifyNoInteractions(pointHistoryService);
    }

    @Test
    @DisplayName("orderId가 null이면 서비스는 호출하지 않는다")
    void onMessage_orderIdNull_returnsWithoutCallingService() {
        OrderCanceledEvent event = new OrderCanceledEvent(
                1L,
                null,
                100,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> consumer.onMessage(event));
        verifyNoInteractions(pointHistoryService);
    }

    @Test
    @DisplayName("userId와 orderId가 모두 null이면 서비스 호출 없이 종료된다")
    void onMessage_userIdAndOrderIdNull_returnsWithoutCallingService() {
        OrderCanceledEvent event = new OrderCanceledEvent(
                null,
                null,
                100,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> consumer.onMessage(event));
        verifyNoInteractions(pointHistoryService);
    }

    @Test
    @DisplayName("정상 이벤트면 useCancel(orderId, userId)가 1회 호출된다")
    void onMessage_validEvent_callsUseCancelOnce() {
        Long userId = 1L;
        Long orderId = 10L;

        OrderCanceledEvent event = new OrderCanceledEvent(
                userId,
                orderId,
                100,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> consumer.onMessage(event));

        verify(pointHistoryService, times(1)).useCancel(orderId, userId);
        verifyNoMoreInteractions(pointHistoryService);
    }

    @Test
    @DisplayName("useCancel에서 예외 발생 시 예외를 그대로 재전파한다")
    void onMessage_serviceThrows_rethrowsException() {
        Long userId = 1L;
        Long orderId = 10L;

        OrderCanceledEvent event = new OrderCanceledEvent(
                userId,
                orderId,
                100,
                LocalDateTime.now()
        );

        RuntimeException exception = new RuntimeException("boom");
        doThrow(exception).when(pointHistoryService).useCancel(orderId, userId);

        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> consumer.onMessage(event));

        assertSame(exception, thrown);
        verify(pointHistoryService, times(1)).useCancel(orderId, userId);
        verifyNoMoreInteractions(pointHistoryService);
    }
}
