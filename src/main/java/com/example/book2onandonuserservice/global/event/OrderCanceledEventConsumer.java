package com.example.book2onandonuserservice.global.event;


import static com.example.book2onandonuserservice.global.config.RabbitConfig.POINT_ORDER_CANCELED_QUEUE;

import com.example.book2onandonuserservice.point.domain.event.OrderCanceledEvent;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledEventConsumer {

    private final PointHistoryService pointHistoryService;

    @RabbitListener(queues = POINT_ORDER_CANCELED_QUEUE)
    public void onMessage(OrderCanceledEvent event) {
        // null 방어
        if (event == null) {
            return;
        }

        Long userId = event.userId();
        Long orderId = event.orderId();

        if (userId == null || orderId == null) {
            log.error("주문취소 이벤트 필드 누락: event={}", event);
            return;
        }

        try {
            pointHistoryService.useCancel(orderId, userId);

            log.info("주문취소 이벤트 처리 성공: orderId={}, userId={}", orderId, userId);
        } catch (Exception e) {
            // 여기서 실패하면 “결제는 성공했는데 포인트는 안 깎임” 문제가 생길 수 있음.
            // 따라서 결제 서비스/오케스트레이션 레벨에서
            // (1) 결제 확정 전에 포인트 차감 성공을 선조건으로 두거나
            // (2) 실패 시 보정 프로세스(관리자/배치)를 둬야 함.
            log.error("주문취소 이벤트 처리 실패: orderId={}, userId={}, msg={}", orderId, userId, e.getMessage(), e);

            // 재시도를 원하면 throw 해서 메시지가 다시 들어오게(설정 필요)
            throw e;
        }
    }
}