package com.example.book2onandonuserservice.global.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.book2onandonuserservice.global.client")
@EnableDiscoveryClient
public class AppConfig {
}