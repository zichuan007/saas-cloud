package com.saas.cloud.common.data.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.saas.cloud.common.security.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Set;

/**
 * 租户 SQL 拦截器：自动为 SELECT/UPDATE/DELETE 追加 tenant_id 条件
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
public class TenantSqlInterceptor implements InnerInterceptor {

    private static final String TENANT_COLUMN = "tenant_id";

    private static final Set<String> IGNORE_TABLES = Set.of(
            "sys_menu",
            "sys_package",
            "sys_tenant",
            "sys_platform_user",
            "sys_announcement",
            "sys_global_config",
            "notify_template"
    );

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
            if (statement instanceof Select) {
                Select select = (Select) statement;
                if (select.getSelectBody() instanceof PlainSelect) {
                    PlainSelect ps = (PlainSelect) select.getSelectBody();
                    if (ps.getFromItem() instanceof Table) {
                        Table table = (Table) ps.getFromItem();
                        if (shouldIgnore(table.getName())) {
                            return;
                        }
                        EqualsTo tenantCondition = buildTenantCondition(table, tenantId);
                        if (ps.getWhere() == null) {
                            ps.setWhere(tenantCondition);
                        } else {
                            ps.setWhere(new AndExpression(ps.getWhere(), tenantCondition));
                        }
                        FieldUtil.setSql(boundSql, select.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("租户拦截器解析 SQL 失败: {}", e.getMessage());
        }
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        try {
            BoundSql boundSql = ms.getBoundSql(parameter);
            Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
            if (statement instanceof Update) {
                Update update = (Update) statement;
                if (shouldIgnore(update.getTable().getName())) {
                    return;
                }
                EqualsTo tenantCondition = buildTenantCondition(update.getTable(), tenantId);
                if (update.getWhere() == null) {
                    update.setWhere(tenantCondition);
                } else {
                    update.setWhere(new AndExpression(update.getWhere(), tenantCondition));
                }
                FieldUtil.setSql(boundSql, update.toString());
            } else if (statement instanceof Delete) {
                Delete delete = (Delete) statement;
                if (shouldIgnore(delete.getTable().getName())) {
                    return;
                }
                EqualsTo tenantCondition = buildTenantCondition(delete.getTable(), tenantId);
                if (delete.getWhere() == null) {
                    delete.setWhere(tenantCondition);
                } else {
                    delete.setWhere(new AndExpression(delete.getWhere(), tenantCondition));
                }
                FieldUtil.setSql(boundSql, delete.toString());
            }
        } catch (Exception e) {
            log.warn("租户拦截器解析 SQL 失败: {}", e.getMessage());
        }
    }

    private EqualsTo buildTenantCondition(Table table, Long tenantId) {
        EqualsTo equalsTo = new EqualsTo();
        String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();
        equalsTo.setLeftExpression(new Column(alias + "." + TENANT_COLUMN));
        equalsTo.setRightExpression(new LongValue(tenantId));
        return equalsTo;
    }

    private boolean shouldIgnore(String tableName) {
        return IGNORE_TABLES.contains(tableName.toLowerCase());
    }

    private static class FieldUtil {
        static void setSql(BoundSql boundSql, String sql) {
            try {
                java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, sql);
            } catch (Exception e) {
                log.error("反射设置 SQL 失败", e);
            }
        }
    }
}
