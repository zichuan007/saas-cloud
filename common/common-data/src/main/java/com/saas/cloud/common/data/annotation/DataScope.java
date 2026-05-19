package com.saas.cloud.common.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据范围注解，标注在 Service 方法上，由 AOP 切面拦截后
 * 通过 ThreadLocal 传递给 DataScopeSqlInterceptor 进行 SQL 条件注入
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 部门表别名，用于拼接 SQL 中的 dept_id 条件。
     * 默认空串表示不加表别名前缀
     */
    String deptAlias() default "";

    /**
     * 用户表别名，用于拼接 SQL 中的 create_user_id 条件。
     * 默认空串表示不加表别名前缀
     */
    String userAlias() default "";
}
