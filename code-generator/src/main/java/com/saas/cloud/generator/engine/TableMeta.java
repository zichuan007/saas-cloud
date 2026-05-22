package com.saas.cloud.generator.engine;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * 数据库表元数据
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TableMeta {

    /** 原始表名，如 sys_user */
    private String tableName;

    /** 实体类名（去前缀+大驼峰），如 User */
    private String entityName;

    /** 实体类名首字母小写，如 user */
    private String entityLowerFirst;

    /** 表注释 */
    private String comment;

    /** 全部字段 */
    private List<FieldMeta> allFields;

    /** 业务字段（排除审计/公共字段） */
    private List<FieldMeta> businessFields;

    /** 适合作为查询条件的字段（String/Integer/Long） */
    private List<FieldMeta> queryFields;

    /** 业务字段需要 import 的类型 */
    private Set<String> fieldImports;

    /** 全部字段需要 import 的类型 */
    private Set<String> allFieldImports;
}
