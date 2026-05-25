package com.saas.cloud.common.security.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetTenantId() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId(100L);
        info.setTenantName("测试租户");

        TenantContext.set(info);

        assertThat(TenantContext.getTenantId()).isEqualTo(100L);
        assertThat(TenantContext.get().getTenantName()).isEqualTo("测试租户");
    }

    @Test
    void shouldReturnNullWhenNotSet() {
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void shouldClearContext() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId(100L);
        TenantContext.set(info);
        TenantContext.setIgnoreTenant(true);

        TenantContext.clear();

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.isIgnoreTenant()).isFalse();
    }

    @Test
    void shouldIgnoreTenantFilter() {
        assertThat(TenantContext.isIgnoreTenant()).isFalse();

        TenantContext.setIgnoreTenant(true);
        assertThat(TenantContext.isIgnoreTenant()).isTrue();

        TenantContext.clearIgnoreTenant();
        assertThat(TenantContext.isIgnoreTenant()).isFalse();
    }

    @Test
    void executeWithoutTenantShouldRestoreState() {
        assertThat(TenantContext.isIgnoreTenant()).isFalse();

        String result = TenantContext.executeWithoutTenant(() -> {
            assertThat(TenantContext.isIgnoreTenant()).isTrue();
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(TenantContext.isIgnoreTenant()).isFalse();
    }

    @Test
    void executeWithoutTenantRunnableShouldRestoreState() {
        assertThat(TenantContext.isIgnoreTenant()).isFalse();

        TenantContext.executeWithoutTenant(() -> {
            assertThat(TenantContext.isIgnoreTenant()).isTrue();
        });

        assertThat(TenantContext.isIgnoreTenant()).isFalse();
    }

    @Test
    void nestedExecuteWithoutTenantShouldKeepIgnoreState() {
        TenantContext.executeWithoutTenant(() -> {
            assertThat(TenantContext.isIgnoreTenant()).isTrue();
            TenantContext.executeWithoutTenant(() -> {
                assertThat(TenantContext.isIgnoreTenant()).isTrue();
            });
            assertThat(TenantContext.isIgnoreTenant()).isTrue();
        });

        assertThat(TenantContext.isIgnoreTenant()).isFalse();
    }
}
