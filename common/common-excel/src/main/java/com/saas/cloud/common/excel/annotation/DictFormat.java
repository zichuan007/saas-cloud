package com.saas.cloud.common.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 字典格式化注解
 * <p>标注在 VO 字段上，导出时将字典值自动转为中文标签，导入时将中文标签转回字典值。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictFormat {

    /**
     * 字典类型编码
     */
    String value();
}
