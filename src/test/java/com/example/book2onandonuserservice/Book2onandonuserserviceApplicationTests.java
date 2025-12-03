package com.example.book2onandonuserservice;

import com.example.book2onandonuserservice.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {

        // 1. 인프라 (Eureka, Config Server) 연결 끄기
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:file:.",
        "eureka.client.enabled=false",

        // 2. 데이터베이스 (MySQL 대신 H2 인메모리 DB 사용)
        // 실제 DB에 붙으면 데이터가 꼬이거나 연결 에러가 날 수 있으므로 H2를 씁니다.
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // 3. Redis (가짜 설정 - 실제 연결 안 해도 빈 생성만 되면 됨)
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=",
        "spring.data.redis.repositories.enabled=false",

        // 4. Encryption 더미 키
        "encryption.secret-key=12345678901234567890123456789012",

        // 5. JWT
        "jwt.secret-key=test-jwt-secret-key-must-be-at-least-32-bytes-long",
        "jwt.access-token-validity=1800",
        "jwt.refresh-token-validity=604800",

        // 7. Mail 더미
        "spring.mail.host=localhost",
        "spring.mail.port=1025",
        "spring.mail.username=test",
        "spring.mail.password=test",

        // 8. 스케줄러 끄기
        "spring.task.scheduling.enabled=false",

        "PAYCO_CLIENT_ID=test-payco-id",
        "PAYCO_CLIENT_SECRET=test-payco-secret",
        "PAYCO_REDIRECT_URI=http://localhost:8080/dummy"

})
class Book2onandonuserserviceApplicationTests {

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void contextLoads() {
        //
    }
}
