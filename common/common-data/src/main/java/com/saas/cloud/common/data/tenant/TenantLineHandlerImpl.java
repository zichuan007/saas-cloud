package com.saas.cloud.common.data.tenant;

import java.util.HashSet;
import java.util.Set;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * MyBatis-Plus 多租户处理器
 * <p>
 * 替代自定义 TenantSqlInterceptor，使用 MP 官方 TenantLineInnerInterceptor，
 * 完整支持 SELECT/INSERT/UPDATE/DELETE 以及 JOIN/子查询/UNION 场景。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
public class TenantLineHandlerImpl implements TenantLineHandler {

    private final Set<String> ignoreTableSet;
    private final String tenantColumn;

    public TenantLineHandlerImpl(TenantProperties tenantProperties) {
        this.ignoreTableSet = new HashSet<>(tenantProperties.getIgnoreTables());
        this.tenantColumn = tenantProperties.getColumn();
    }

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        // 未登录或平台管理端请求，返回不存在的值防止数据泄露
        log.debug("租户上下文为空，返回默认值 -1，将无法查询到任何数据");
        return new LongValue(-1L);
    }

    @Override
    public String getTenantIdColumn() {
        return tenantColumn;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 1. 编程式忽略（超级管理员/跨租户操作）
        if (TenantContext.isIgnoreTenant()) {
            log.debug("忽略租户过滤（编程式），表: {}", tableName);
            return true;
        }

        // 2. 未登录时不注入租户条件（平台管理端请求）
        if (TenantContext.getTenantId() == null) {
            return true;
        }

        // 3. 配置的忽略表
        boolean ignore = ignoreTableSet.contains(tableName.toLowerCase());
        if (ignore) {
            log.debug("表 {} 在忽略列表中，不进行租户过滤", tableName);
        }

        return ignore;
    }
}
