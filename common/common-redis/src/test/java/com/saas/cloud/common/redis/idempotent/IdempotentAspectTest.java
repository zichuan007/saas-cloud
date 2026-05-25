package com.saas.cloud.common.redis.idempotent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.saas.cloud.common.core.exception.BusinessException;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class IdempotentAspectTest {

    private RedissonClient redissonClient;
    private RLock lock;
    private IdempotentAspect aspect;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        lock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        aspect = new IdempotentAspect(redissonClient);
    }

    @Idempotent(key = "'test-key'")
    public void testMethod() {
    }

    private Idempotent getAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("testMethod");
        return method.getAnnotation(Idempotent.class);
    }

    @Test
    void shouldProceedWhenLockAcquired() throws Throwable {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        ProceedingJoinPoint joinPoint = mockJoinPoint();
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.around(joinPoint, getAnnotation());

        assertThat(result).isEqualTo("result");
        verify(lock).unlock();
    }

    @Test
    void shouldThrowWhenLockNotAcquired() throws Throwable {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        ProceedingJoinPoint joinPoint = mockJoinPoint();

        assertThatThrownBy(() -> aspect.around(joinPoint, getAnnotation()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请勿重复提交");

        verify(joinPoint, never()).proceed();
    }

    private ProceedingJoinPoint mockJoinPoint() throws NoSuchMethodException {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getClass().getMethod("testMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        return joinPoint;
    }
}
