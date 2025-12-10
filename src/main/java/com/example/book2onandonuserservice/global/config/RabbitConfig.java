package com.example.book2onandonuserservice.global.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "book2.dev.user.exchange";
    public static final String ROUTING_KEY_WELCOME = "coupon.welcome";
    public static final String ROUTING_KEY_BIRTHDAY = "coupon.birthday"; //이거 추가

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }
}