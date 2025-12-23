package com.example.book2onandonuserservice.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "book2.dev.user.exchange";
    public static final String ROUTING_KEY_WELCOME = "coupon.welcome";
    public static final String ROUTING_KEY_BIRTHDAY = "coupon.birthday"; //이거 추가

    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange"; // Exchange
    public static final String ORDER_CANCELED_ROUTING_KEY = "order.canceled"; // Routing Key
    public static final String POINT_ORDER_CANCELED_QUEUE = "point.order.canceled.queue"; // Queue

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 쿠폰
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // 주문/결제 이벤트
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue pointOrderCanceledQueue() {
        return QueueBuilder.durable(POINT_ORDER_CANCELED_QUEUE).build();
    }

    @Bean
    public Binding pointOrderCanceledBinding(DirectExchange orderEventExchange, Queue pointOrderCanceledQueue) {
        return BindingBuilder
                .bind(pointOrderCanceledQueue)
                .to(orderEventExchange)
                .with(ORDER_CANCELED_ROUTING_KEY);
    }
}