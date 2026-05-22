package com.saas.cloud.common.data.interceptor;

import lombok.Data;

/**
 * DataScope 上下文持有类，基于 ThreadLocal 存储当前方法的数据范围注解信息。
 * 由 DataScopeAspect 设置，由 DataScopeSqlInterceptor 读取
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<DataScopeParam> CONTEXT = new ThreadLocal<>();

    private DataScopeContextHolder() {
    }

    /**
     * 设置当前线程的数据范围参数
     *
     * @param param 数据范围参数
     */
    public static void set(DataScopeParam param) {
        CONTEXT.set(param);
    }

    /**
     * 获取当前线程的数据范围参数
     *
     * @return 数据范围参数，可能为 null
     */
    public static DataScopeParam get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程的数据范围参数
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 数据范围参数，承载 @DataScope 注解中的属性值
     */
    @Data
    public static class DataScopeParam {

        /** 部门表别名 */
        private String deptAlias;

        /** 用户表别名 */
        private String userAlias;

        public DataScopeParam(String deptAlias, String userAlias) {
            this.deptAlias = deptAlias;
            this.userAlias = userAlias;
        }
    }
}
