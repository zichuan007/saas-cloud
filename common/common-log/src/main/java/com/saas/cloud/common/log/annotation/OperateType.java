package com.saas.cloud.common.log.annotation;

/**
 * 操作类型枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public enum OperateType {

    /** 其他 */
    OTHER,

    /** 查询 */
    QUERY,

    /** 新增 */
    CREATE,

    /** 修改 */
    UPDATE,

    /** 删除 */
    DELETE,

    /** 导出 */
    EXPORT,

    /** 导入 */
    IMPORT,

    /** 登录 */
    LOGIN,

    /** 登出 */
    LOGOUT;
}
