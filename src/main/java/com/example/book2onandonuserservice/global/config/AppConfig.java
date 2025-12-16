package com.example.book2onandonuserservice.global.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisRepositories
public class AppConfig {
}