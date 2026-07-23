package com.saas.cloud.common.data.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.saas.cloud.common.security.context.TenantContext;

import net.sf.jsqlparser.expression.LongValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多租户处理器单测：验证 fail-closed 行为
 *
 * @author saas-cloud
 * @since 2026-07-22
 */
class TenantLineHandlerImplTest {

    private final TenantProperties properties = new TenantProperties();
    private final TenantLineHandlerImpl handler = new TenantLineHandlerImpl(properties);

    @AfterEach
    void clear() {
        TenantContext.clear();
        TenantContext.clearIgnoreTenant();
    }

    @Test
    void getTenantIdReturnsMinusOneWhenContextNull() {
        // fail-closed：无上下文返回 -1，查不到数据，不泄露
        assertEquals(-1L, ((LongValue) handler.getTenantId()).getValue());
    }

    @Test
    void getTenantIdReturnsContextValue() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId(100L);
        TenantContext.set(info);
        assertEquals(100L, ((LongValue) handler.getTenantId()).getValue());
    }

    @Test
    void ignoreTableReturnsTrueForConfiguredIgnore() {
        assertTrue(handler.ignoreTable("sys_menu"));
        assertTrue(handler.ignoreTable("SYS_MENU"));
    }

    @Test
    void ignoreTableReturnsFalseForTenantTableWhenNoContext() {
        // fail-closed：租户维度表 + 无上下文 → 不放行（交由 getTenantId 返回 -1）
        assertFalse(handler.ignoreTable("sys_user"));
    }

    @Test
    void ignoreTableReturnsTrueWhenIgnoreTenantFlag() {
        TenantContext.setIgnoreTenant(true);
        assertTrue(handler.ignoreTable("sys_user"));
    }

    @Test
    void ignoreTableReturnsFalseForTenantTableWhenContextSet() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId(100L);
        TenantContext.set(info);
        // 有上下文，租户维度表正常过滤
        assertFalse(handler.ignoreTable("sys_user"));
    }
}
