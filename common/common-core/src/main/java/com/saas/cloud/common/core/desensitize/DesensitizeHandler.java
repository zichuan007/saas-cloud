package com.saas.cloud.common.core.desensitize;

import java.lang.annotation.Annotation;

/**
 * 数据脱敏处理器接口
 * <p>每种脱敏策略（手机号、身份证等）实现此接口，定义具体的脱敏规则。</p>
 *
 * @param <T> 对应的脱敏注解类型
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface DesensitizeHandler<T extends Annotation> {

    /**
     * 对原始字符串执行脱敏
     *
     * @param origin     原始值
     * @param annotation 字段上的脱敏注解实例（可用于读取注解属性）
     * @return 脱敏后的字符串
     */
    String desensitize(String origin, T annotation);
}
