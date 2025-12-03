package com.example.book2onandonuserservice.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.util.RedisUtil;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisUtilTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisUtil redisUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        redisUtil = new RedisUtil(redisTemplate);

        // redisTemplate.opsForValue()가 valueOperations를 반환하도록 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("setBlackList() - 블랙리스트 저장 성공")
    void setBlackList_success() {
        redisUtil.setBlackList("BLACKLIST:token", "logout", 1000L);

        verify(valueOperations)
                .set("BLACKLIST:token", "logout", 1000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("hasKeyBlackList() - 키 존재 여부 확인")
    void hasKeyBlackList_success() {
        when(redisTemplate.hasKey("BLACKLIST:123")).thenReturn(true);

        boolean result = redisUtil.hasKeyBlackList("BLACKLIST:123");

        assertThat(result).isTrue();
        verify(redisTemplate).hasKey("BLACKLIST:123");
    }

    @Test
    @DisplayName("setData() - 인증번호 저장 성공")
    void setData_success() {
        redisUtil.setData("EMAIL:code", "123456", 5000L);

        verify(valueOperations)
                .set("EMAIL:code", "123456", 5000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("getData() - 값 조회")
    void getData_success() {
        when(valueOperations.get("EMAIL:code")).thenReturn("123456");

        String value = redisUtil.getData("EMAIL:code");

        assertThat(value).isEqualTo("123456");
    }

    @Test
    @DisplayName("deleteData() - 키 삭제")
    void deleteData_success() {
        redisUtil.deleteData("EMAIL:code");

        verify(redisTemplate).delete("EMAIL:code");
    }
}
