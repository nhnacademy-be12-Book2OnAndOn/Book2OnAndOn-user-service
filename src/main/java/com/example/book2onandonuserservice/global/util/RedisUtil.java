package com.example.book2onandonuserservice.global.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;

    public void setBlackList(String key, Object o, Long minutes) {
        redisTemplate.opsForValue().set(key, o.toString(), minutes, TimeUnit.MILLISECONDS);
    }

    public boolean hasKeyBlackList(String key) {
        return redisTemplate.hasKey(key);
    }

    //인증번호 저장을 위한 데이터 저장 로직
    public void setData(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
