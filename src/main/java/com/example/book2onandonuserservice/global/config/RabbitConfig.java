package com.example.book2onandonuserservice.global.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "book2.dev.user.exchange";
    public static final String ROUTING_KEY = "coupon.welcome";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }
}