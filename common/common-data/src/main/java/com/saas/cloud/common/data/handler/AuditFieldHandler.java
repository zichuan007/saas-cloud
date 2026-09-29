package com.saas.cloud.common.data.handler;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;

/**
 * 审计字段自动填充
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Component
public class AuditFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        UserContext.UserInfo user = UserContext.get();
        if (user != null) {
            this.strictInsertFill(metaObject, "createUserId", String.class, String.valueOf(user.getUserId()));
            this.strictInsertFill(metaObject, "createUserName", String.class, user.getUsername());
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            this.strictInsertFill(metaObject, "tenantId", Long.class, tenantId);
        }

        // 有效状态默认置 1
        this.strictInsertFill(metaObject, "validStatus", Integer.class, 1);
        // 链路追踪ID 从 MDC 注入（Micrometer Tracing 运行时写入）
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isEmpty()) {
            this.strictInsertFill(metaObject, "traceId", String.class, traceId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        UserContext.UserInfo user = UserContext.get();
        if (user != null) {
            this.strictUpdateFill(metaObject, "updateUserId", String.class, String.valueOf(user.getUserId()));
            this.strictUpdateFill(metaObject, "updateUserName", String.class, user.getUsername());
        }
    }
}
