package com.example.book2onandonuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class Book2onandonuserserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Book2onandonuserserviceApplication.class, args);
    }

}
