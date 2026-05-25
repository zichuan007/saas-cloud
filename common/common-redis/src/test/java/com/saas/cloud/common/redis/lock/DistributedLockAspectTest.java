package com.saas.cloud.common.redis.lock;

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
class DistributedLockAspectTest {

    private RedissonClient redissonClient;
    private RLock lock;
    private DistributedLockAspect aspect;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        lock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        aspect = new DistributedLockAspect(redissonClient);
    }

    @DistributedLock(key = "'test-lock'")
    public void lockMethod() {
    }

    private DistributedLock getAnnotation() throws NoSuchMethodException {
        Method method = getClass().getMethod("lockMethod");
        return method.getAnnotation(DistributedLock.class);
    }

    @Test
    void shouldProceedWhenLockAcquired() throws Throwable {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        ProceedingJoinPoint joinPoint = mockJoinPoint();
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, getAnnotation());

        assertThat(result).isEqualTo("ok");
        verify(lock).unlock();
    }

    @Test
    void shouldThrowWhenLockNotAcquired() throws Throwable {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        ProceedingJoinPoint joinPoint = mockJoinPoint();

        assertThatThrownBy(() -> aspect.around(joinPoint, getAnnotation()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("操作过于频繁，请稍后再试");

        verify(joinPoint, never()).proceed();
    }

    @Test
    void shouldReleaseOnlyWhenHeldByCurrentThread() throws Throwable {
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        ProceedingJoinPoint joinPoint = mockJoinPoint();
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.around(joinPoint, getAnnotation());

        verify(lock, never()).unlock();
    }

    private ProceedingJoinPoint mockJoinPoint() throws NoSuchMethodException {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getClass().getMethod("lockMethod"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        return joinPoint;
    }
}
