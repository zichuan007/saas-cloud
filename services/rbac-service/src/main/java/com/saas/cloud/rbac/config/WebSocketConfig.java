package com.saas.cloud.rbac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saas.cloud.common.websocket.interceptor.WebSocketTokenResolver;

import cn.dev33.satoken.stp.StpUtil;

/**
 * WebSocket 配置
 * <p>提供 Sa-Token 的 WebSocketTokenResolver 实现，激活 common-websocket 自动配置。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketTokenResolver webSocketTokenResolver() {
        return token -> {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return null;
            }
            WebSocketTokenResolver.TokenInfo tokenInfo = new WebSocketTokenResolver.TokenInfo();
            tokenInfo.setUserId(Long.parseLong(loginId.toString()));

            cn.dev33.satoken.session.SaSession session = StpUtil.getSessionByLoginId(loginId, false);
            if (session != null) {
                Long tenantId = session.get("tenantId", null);
                tokenInfo.setTenantId(tenantId);
            }
            return tokenInfo;
        };
    }
}
