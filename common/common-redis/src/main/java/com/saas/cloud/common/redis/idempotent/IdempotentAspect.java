package com.saas.cloud.common.redis.idempotent;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.core.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 幂等性切面
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String idempotentKey = "idempotent:" + parseSpel(idempotent.key(), joinPoint);
        RLock lock = redissonClient.getLock(idempotentKey);

        boolean acquired = lock.tryLock(0, idempotent.timeout(), idempotent.timeUnit());
        if (!acquired) {
            throw new BusinessException(idempotent.message());
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String parseSpel(String spel, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || paramNames.length == 0) {
            return spel;
        }

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            ((StandardEvaluationContext) context).setVariable(paramNames[i], args[i]);
        }

        try {
            Object value = PARSER.parseExpression(spel).getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.warn("SpEL 解析失败, 表达式={}, 使用原始值", spel);
            return spel;
        }
    }
}
