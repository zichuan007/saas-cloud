package com.saas.cloud.common.data.interceptor;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.saas.cloud.common.core.enums.DataScopeEnum;
import com.saas.cloud.common.data.interceptor.DataScopeContextHolder.DataScopeParam;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.common.security.context.UserContext.UserInfo;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * 数据范围 SQL 拦截器：根据 UserContext 中的数据范围配置，
 * 自动为 SELECT 语句追加 dept_id / create_user_id 条件，
 * 实现不同角色看到不同范围的数据
 * 依赖 DataScopeAspect 通过 ThreadLocal 传递 @DataScope 注解参数，
 * 只有 ThreadLocal 中存在参数时才会注入 SQL 条件
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
public class DataScopeSqlInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 检查 ThreadLocal 中是否有 @DataScope 注解参数
        DataScopeParam scopeParam = DataScopeContextHolder.get();
        if (scopeParam == null) {
            return;
        }

        // 获取当前用户信息
        UserInfo userInfo = UserContext.get();
        if (userInfo == null) {
            return;
        }

        // 超级管理员或全部数据范围，不追加条件
        Integer roleLevel = userInfo.getRoleLevel();
        if (roleLevel != null && roleLevel == 0) {
            return;
        }
        Integer dataScope = userInfo.getDataScope();
        if (dataScope == null || dataScope == DataScopeEnum.ALL.getCode()) {
            return;
        }

        // 构建数据范围条件
        Expression scopeCondition = buildScopeCondition(dataScope, userInfo, scopeParam);
        if (scopeCondition == null) {
            return;
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
            if (statement instanceof PlainSelect) {
                PlainSelect ps = (PlainSelect) statement;
                if (ps.getWhere() == null) {
                    ps.setWhere(scopeCondition);
                } else {
                    ps.setWhere(new AndExpression(ps.getWhere(), scopeCondition));
                }
                setSql(boundSql, ps.toString());
                log.debug("数据范围拦截器注入 SQL 条件, dataScope={}, sql={}", dataScope, ps);
            }
        } catch (Exception e) {
            log.warn("数据范围拦截器解析 SQL 失败: {}", e.getMessage());
        }
    }

    /**
     * 根据数据范围类型构建不同的 SQL 条件表达式
     *
     * @param dataScope  数据范围编码
     * @param userInfo   当前用户信息
     * @param scopeParam 注解参数（表别名等）
     * @return SQL 条件表达式，返回 null 表示不追加条件
     */
    private Expression buildScopeCondition(int dataScope, UserInfo userInfo, DataScopeParam scopeParam) {
        if (dataScope == DataScopeEnum.DEPT_AND_CHILDREN.getCode()) {
            return buildDeptAndChildrenCondition(userInfo, scopeParam);
        } else if (dataScope == DataScopeEnum.DEPT.getCode()) {
            return buildDeptCondition(userInfo, scopeParam);
        } else if (dataScope == DataScopeEnum.SELF.getCode()) {
            return buildSelfCondition(userInfo, scopeParam);
        } else if (dataScope == DataScopeEnum.CUSTOM.getCode()) {
            return buildCustomCondition(userInfo, scopeParam);
        }
        return null;
    }

    /**
     * 本部门及下级：dept_id IN (SELECT id FROM sys_dept WHERE FIND_IN_SET(当前deptId, ancestors) OR id = 当前deptId)
     */
    private Expression buildDeptAndChildrenCondition(UserInfo userInfo, DataScopeParam scopeParam) {
        Long deptId = userInfo.getDeptId();
        if (deptId == null) {
            log.warn("用户 {} 的部门ID为空，无法构建本部门及下级数据范围条件", userInfo.getUserId());
            return null;
        }
        String deptColumn = buildColumnName(scopeParam.getDeptAlias(), "dept_id");
        // 子查询: SELECT id FROM sys_dept WHERE FIND_IN_SET(deptId, ancestors) OR id = deptId
        String subQuery = String.format(
                "SELECT id FROM sys_dept WHERE FIND_IN_SET(%d, ancestors) OR id = %d",
                deptId, deptId
        );
        return buildInSubSelect(deptColumn, subQuery);
    }

    /**
     * 本部门：dept_id = 当前deptId
     */
    private Expression buildDeptCondition(UserInfo userInfo, DataScopeParam scopeParam) {
        Long deptId = userInfo.getDeptId();
        if (deptId == null) {
            log.warn("用户 {} 的部门ID为空，无法构建本部门数据范围条件", userInfo.getUserId());
            return null;
        }
        String deptColumn = buildColumnName(scopeParam.getDeptAlias(), "dept_id");
        return new EqualsTo(new Column(deptColumn), new LongValue(deptId));
    }

    /**
     * 仅本人：create_user_id = 当前userId
     */
    private Expression buildSelfCondition(UserInfo userInfo, DataScopeParam scopeParam) {
        Long userId = userInfo.getUserId();
        if (userId == null) {
            log.warn("用户ID为空，无法构建仅本人数据范围条件");
            return null;
        }
        String userColumn = buildColumnName(scopeParam.getUserAlias(), "create_user_id");
        return new EqualsTo(new Column(userColumn), new LongValue(userId));
    }

    /**
     * 自定义：dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id IN
     *         (SELECT role_id FROM sys_user_role WHERE user_id = 当前userId))
     */
    private Expression buildCustomCondition(UserInfo userInfo, DataScopeParam scopeParam) {
        Long userId = userInfo.getUserId();
        if (userId == null) {
            log.warn("用户ID为空，无法构建自定义数据范围条件");
            return null;
        }
        String deptColumn = buildColumnName(scopeParam.getDeptAlias(), "dept_id");
        // 嵌套子查询：通过 sys_user_role 找到用户的角色，再通过 sys_role_dept 找到角色关联的部门
        String subQuery = String.format(
                "SELECT dept_id FROM sys_role_dept WHERE role_id IN (SELECT role_id FROM sys_user_role WHERE user_id = %d)",
                userId
        );
        return buildInSubSelect(deptColumn, subQuery);
    }

    /**
     * 构建 column IN (subQuery) 表达式
     *
     * @param columnName 列名（可能带别名前缀）
     * @param subQuery   子查询 SQL
     * @return IN 表达式
     */
    private Expression buildInSubSelect(String columnName, String subQuery) {
        try {
            Statement subStatement = CCJSqlParserUtil.parse(subQuery);
            if (subStatement instanceof Select) {
                ParenthesedSelect parenthesedSelect = new ParenthesedSelect();
                parenthesedSelect.setSelect((Select) subStatement);
                InExpression inExpression = new InExpression(new Column(columnName), parenthesedSelect);
                return inExpression;
            }
            return null;
        } catch (Exception e) {
            log.error("构建 IN 子查询失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建列名，如果有别名则加上 "alias." 前缀
     *
     * @param alias      表别名
     * @param columnName 列名
     * @return 完整列名
     */
    private String buildColumnName(String alias, String columnName) {
        if (alias != null && !alias.isEmpty()) {
            return alias + "." + columnName;
        }
        return columnName;
    }

    /**
     * 通过反射设置 BoundSql 中的 SQL 语句
     *
     * @param boundSql BoundSql 对象
     * @param sql      新的 SQL 语句
     */
    private static void setSql(BoundSql boundSql, String sql) {
        try {
            Field field = BoundSql.class.getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, sql);
        } catch (Exception e) {
            log.error("反射设置 SQL 失败", e);
        }
    }
}
