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
        // fail-closed：无租户上下文返回不存在的值，防止跨租户数据泄露。
        // 走到这里说明有未标注 @TenantIgnore 的合法跨租户操作在无上下文线程上查租户维度表，
        // 以 warn 暴露以便定位（正常请求/启动预热不应命中此处）。
        log.warn("租户上下文为空，返回默认值 -1，查询租户维度表将返回空。需排查是否遗漏 @TenantIgnore",
                new IllegalStateException("tenant context missing"));
        return new LongValue(-1L);
    }

    @Override
    public String getTenantIdColumn() {
        return tenantColumn;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 1. 编程式显式忽略（平台管理端 / 跨租户初始化 / @TenantIgnore）
        if (TenantContext.isIgnoreTenant()) {
            log.debug("忽略租户过滤（编程式），表: {}", tableName);
            return true;
        }

        // 2. fail-closed：无租户上下文时不再放行全表，交由 getTenantId() 返回 -1L，
        //    使租户维度查询查不到任何数据，防止绕过网关的流量全量可见。
        //    平台级查询必须通过 @TenantIgnore 或 executeWithoutTenant 显式提权。

        // 3. 配置的忽略表（平台级共享表）
        boolean ignore = ignoreTableSet.contains(tableName.toLowerCase());
        if (ignore) {
            log.debug("表 {} 在忽略列表中，不进行租户过滤", tableName);
        }
        return ignore;
    }
}
