package com.example.book2onandonuserservice.aop;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.global.annotation.DistributedLock;
import com.example.book2onandonuserservice.global.aop.DistributedLockAop;
import com.example.book2onandonuserservice.global.util.CustomSpringELParser;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class DistributedLockAopTest {
    @InjectMocks
    private DistributedLockAop distributedLockAop;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private CustomSpringELParser customSpringELParser;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private RLock rLock;

    @Test
    @DisplayName("락 획득 성공 시 비즈니스 로직 수행 후 락 해제")
    void lock_Success() throws Throwable {
        Method method = TestService.class.getMethod("testMethod");
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getParameterNames()).willReturn(new String[]{});
        given(joinPoint.getArgs()).willReturn(new Object[]{});

        given(customSpringELParser.getDynamicValue(any(), any(), any())).willReturn("test-key");
        given(redissonClient.getLock(anyString())).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any())).willReturn(true);

        given(rLock.isLocked()).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);

        given(joinPoint.proceed()).willReturn(null);

        // when
        distributedLockAop.lock(joinPoint, annotation);

        // then
        verify(rLock).tryLock(anyLong(), anyLong(), any());
        verify(joinPoint).proceed();
        verify(rLock).unlock();
    }


    @Test
    @DisplayName("락 획득 실패 시 예외 발생")
    void lock_Fail() throws Throwable {
        Method method = TestService.class.getMethod("testMethod");
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        given(joinPoint.getSignature()).willReturn(signature);
        given(customSpringELParser.getDynamicValue(any(), any(), any())).willReturn("test-key");
        given(redissonClient.getLock(anyString())).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any())).willReturn(false); // 락 획득 실패

        assertThatThrownBy(() -> distributedLockAop.lock(joinPoint, annotation))
                .isInstanceOf(IllegalStateException.class);
    }

    static class TestService {
        @DistributedLock(key = "test")
        public void testMethod() {
            throw new UnsupportedOperationException("This method is intentionally not implemented.");
        }
    }
}
