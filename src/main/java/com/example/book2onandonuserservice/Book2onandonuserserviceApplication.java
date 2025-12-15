package com.example.book2onandonuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisRepositories(basePackages = "com.example.book2onandonuserservice.auth.repository")
@SpringBootApplication
public class Book2onandonuserserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Book2onandonuserserviceApplication.class, args);
    }

}
