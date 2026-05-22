package com.saas.cloud.common.data.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.data.annotation.DataScope;
import com.saas.cloud.common.data.interceptor.DataScopeContextHolder;
import com.saas.cloud.common.data.interceptor.DataScopeContextHolder.DataScopeParam;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据范围 AOP 切面，拦截 @DataScope 注解方法，
 * 将注解参数写入 ThreadLocal，供 DataScopeSqlInterceptor 读取
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint point, DataScope dataScope) throws Throwable {
        try {
            DataScopeParam param = new DataScopeParam(dataScope.deptAlias(), dataScope.userAlias());
            DataScopeContextHolder.set(param);
            log.debug("设置数据范围上下文: deptAlias={}, userAlias={}", param.getDeptAlias(), param.getUserAlias());
            return point.proceed();
        } finally {
            DataScopeContextHolder.clear();
            log.debug("清除数据范围上下文");
        }
    }
}
