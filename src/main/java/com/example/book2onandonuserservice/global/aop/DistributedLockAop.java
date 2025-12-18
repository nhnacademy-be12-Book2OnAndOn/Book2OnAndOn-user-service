package com.example.book2onandonuserservice.global.aop;

import com.example.book2onandonuserservice.global.annotation.DistributedLock;
import com.example.book2onandonuserservice.global.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)// @Transactional 보다 먼저 락이 걸려야함.
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private final RedissonClient redissonClient;
    private final CustomSpringELParser customSpringELParser;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // SpEL을 사용하여 락 키 생성
        String key = "LOCK: " + customSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        );
        RLock rLock = redissonClient.getLock(key);

        try {
            boolean available = rLock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!available) {
                log.warn("Redisson Lock 획득 식패 - key: {}", key);
                throw new IllegalStateException("현재 처리 중인 요청입니다. 잠시 후 다시 시도해주세요.");
            }

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }
}
