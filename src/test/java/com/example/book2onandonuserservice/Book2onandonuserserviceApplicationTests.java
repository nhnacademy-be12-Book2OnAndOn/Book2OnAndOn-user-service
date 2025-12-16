package com.example.book2onandonuserservice;

import com.example.book2onandonuserservice.auth.repository.redis.RefreshTokenRepository;
import com.example.book2onandonuserservice.global.client.BookServiceClient;
import com.example.book2onandonuserservice.global.client.OrderServiceClient;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // 기존 설정 유지 ...
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:file:.",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=",
        "encryption.secret-key=12345678901234567890123456789012",
        "jwt.secret-key=test-jwt-secret-key-must-be-at-least-32-bytes-long",
        "jwt.access-token-validity=1800",
        "jwt.refresh-token-validity=604800",
        "spring.mail.host=localhost",
        "spring.mail.port=1025",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "spring.task.scheduling.enabled=false",
        "payco.client-id=test-payco-id",
        "payco.client-secret=test-payco-secret",
        "payco.redirect-uri=http://localhost:8080/dummy",

        "spring.main.allow-bean-definition-overriding=true",

        "management.health.redis.enabled=false"
})
class Book2onandonuserserviceApplicationTests {

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private PaycoClient paycoClient;

    @MockBean
    private OrderServiceClient orderServiceClient;

    @MockBean
    private BookServiceClient bookServiceClient;

    @Test
    void contextLoads() {
        // 이제 성공할 것입니다!
    }
}