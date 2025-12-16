package com.example.book2onandonuserservice.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.example.book2onandonuserservice.auth.repository.redis")
public class RedisConfig {
    //
}