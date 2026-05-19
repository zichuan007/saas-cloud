package com.saas.cloud.generator.engine;

import lombok.Data;

/**
 * 数据库字段元数据
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class FieldMeta {

    /** 数据库列名，如 user_name */
    private String columnName;

    /** Java 属性名（驼峰），如 userName */
    private String propertyName;

    /** 首字母大写属性名，如 UserName（用于生成 getter/setter 调用） */
    private String capitalizedName;

    /** 完整 Java 类型，如 java.time.LocalDateTime */
    private String fullType;

    /** 简短 Java 类型，如 LocalDateTime */
    private String propertyType;

    /** 字段注释 */
    private String comment;

    /** 是否为主键 */
    private boolean primaryKey;

    /** 数据库列类型，如 varchar(64) */
    private String columnType;

    /** 是否允许为空 */
    private boolean nullable;

    /** 字段长度（仅 varchar/char 有值，其他为 0） */
    private int length;
}
