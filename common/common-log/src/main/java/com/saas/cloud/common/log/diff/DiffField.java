package com.saas.cloud.common.log.diff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注实体字段的中文显示名，用于操作日志字段 Diff
 * <p>标注该注解的字段会参与 {@link DiffUtil} 的变更对比，
 * 未标注的字段将被跳过。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiffField {

    /**
     * 字段中文显示名
     */
    String value();
}
