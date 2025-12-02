package com.example.book2onandonuserservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {

        // 1. Config Server, Eureka 끄기
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",

        // 2. H2 DB 사용
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // 3. Redis 더미 설정
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=dummy",

        // 4. Encryption 더미 키
        "encryption.secret-key=12345678901234567890123456789012",

        // 5. JWT
        "jwt.secret-key=test-jwt-secret",
        "jwt.access-token-validity=1800",
        "jwt.refresh-token-validity=604800",

        // 6. Payco 더미
        "payco.client-id=test-client",
        "payco.client-secret=test-secret",

        // 7. Mail 더미
        "spring.mail.host=localhost",
        "spring.mail.port=1025",
        "spring.mail.username=test",
        "spring.mail.password=test",

        // 8. 스케줄러 끄기
        "spring.task.scheduling.enabled=false"

})
class Book2onandonuserserviceApplicationTests {

    @Test
    void contextLoads() {
    }
}
