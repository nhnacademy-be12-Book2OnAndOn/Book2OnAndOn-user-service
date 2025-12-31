package com.example.book2onandonuserservice;

import com.example.book2onandonuserservice.auth.repository.redis.RefreshTokenRepository;
import com.example.book2onandonuserservice.global.client.BookServiceClient;
import com.example.book2onandonuserservice.global.client.OrderServiceClient;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
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
        "encryption.keys.v1=12345678901234567890123456789012",
        "encryption.keys.v2=abcdefabcdefabcdefabcdefabcdef12",
        "encryption.active-version=v1",
        "encryption.hash-secret=hashSecretKeyForTestMustBeLongEnoughValue123",
        "jwt.private-key=MIIEugIBADANBgkqhkiG9w0BAQEFAASCBKQwggSgAgEAAoIBAQCxmaUF5eQL5VYHPWbRo3I4hxHI9QGiEoBKByvK7/HZ1SIgA9uszk/Yo3CVAEYBrn6zzF+VRC1YU76FW2a+sn/8yGYgTjQy3ld0KvR5t2e9sTjqfwhxxnsUmaXkUswnMIPjYQfTJsDFm+84feuI+R1udn8eotGQ6KlKZ9dDCxQo7RiJB5GcmSo5O103mHAjglchFlmCPEwA6kywGgkOWJzNMEX5wc6e+JkCmDHBSFjt+CQR3BLYfm68W2WzRmhn6L+7j0o+Vi6FMVp36kEQiTql/vseFnJrzjchY+7+pKvAWhAWF5rTaNkZmZECRTD1lf3ubEp1T5gHnHlzptrEB+RRAgMBAAECggEAWXnpFDGgVQXz863RsWjBUQ1rvm0Bp5/elm1KePGuTWr9yFdpQ4Sl4aVmQQtkOdCp8PfUaCVzGv8GC7Xi68yOgtKL8Va1IbO3J8XEadYnJAC3hhz+KiQvOk+2rnOwe0YMDHCEOrNZh7VqLnHySo4Hb/GvVcX+Ax3UvLHYV+SrAXqrNgaZMODV7tZnSFqUP5E28SjM4bPAXRoOmXOyywyi8LsPWaKKs8uP57p0xx2FGgrCkcebCqdWcEapS4up61OusAj4aGqomdM1vVy0CcSuUsaUDOIAVe6JsM9I/PxgdrJlq5kfCaZbHUDa/U17qEWm3ju8CVjFj/RUgngUPKzsYQKBgQDgCXLlN4lWuFT2Y/WC2PqFxhDAmRKvezer73WwVPJfp+NXLtgd50tgz+5mkFh5VpuCAiQbIailE2+Y7U+00937x5gj4xZHfJryXZzJmTgbAb7k0Ozt/EWyvfBRJc09gC+bUhF51N7PV/NmOVsmRNHy322l77vKCX2q86Lf/SYtTQKBgQDK8C0kLCrlA1ru/DAAjJ/NfdIlUeu9PBmIXUHcllvRhMZ1RtvTH81SGX47QuguLr1AIegYExzMx5muBZx44LXR1Wt+PNUtchi2eBliY4pPw1mK7nJno+h5uZVwMXeag5EXkg4kEvtS93Ep0npqyZD8f6ligFxgbqc7HkW0/axhFQKBgFh4Yr7Rp5JvSF3kK1d4h7W7+XGVASP67IkhDsCLSwsrKUOMZBuq8sFQWzx1U/rWkKJOZiOwrwBBJJinXgpyf/6lWxcQ3gNs2WNMRFX4A4fmsJN7TZTNQljLWNMyslHPBP248t3BihsnCB8eFziNhLr8MDiO6wFlrhKME42QUjshAoGAKQnaxxbbccZ07CE5f9LwOfj4ty1S08jEjQv21qndVYhZLbwvlgk3VyPqoAdOtpAjG0YADmZHC65NiKB/3P3sJsvW2gdpQLBzCOtOjtP8U1b405yWrBi8WWlLLA4E1s1GWRLjIy5nfoalbsSulsu+MyGJ2m6Ev8eAr/bWN9yYsGUCf0gLRSZXodHIbGGrHsgew/qHQ1WA7GTJ/B5hweKgGRISN5AQ07f3E3ga9VbrHXYkn3ixWUPW06LGxvnSKlAL2sGRvWGCBYPj9p9Ne+lddpiqNt/2T9wFCXr8CsHgq/2sVDsgG4o+wyAXb086hjQdW5bzC/McsQJL2Gzx83FoTWo=",
        "jwt.public-key=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsZmlBeXkC+VWBz1m0aNyOIcRyPUBohKASgcryu/x2dUiIAPbrM5P2KNwlQBGAa5+s8xflUQtWFO+hVtmvrJ//MhmIE40Mt5XdCr0ebdnvbE46n8IccZ7FJml5FLMJzCD42EH0ybAxZvvOH3riPkdbnZ/HqLRkOipSmfXQwsUKO0YiQeRnJkqOTtdN5hwI4JXIRZZgjxMAOpMsBoJDliczTBF+cHOnviZApgxwUhY7fgkEdwS2H5uvFtls0ZoZ+i/u49KPlYuhTFad+pBEIk6pf77HhZya843IWPu/qSrwFoQFhea02jZGZmRAkUw9ZX97mxKdU+YB5x5c6baxAfkUQIDAQAB",
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
    private RedissonClient redissonClient;

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
        // ㅁㄴㅇㄹㅁ
    }
}