package com.saas.cloud.common.websocket.interceptor;

import lombok.Data;

/**
 * WebSocket Token 解析器接口
 * <p>由具体应用实现（如 rbac-service 中通过 Sa-Token 解析），
 * 在 WebSocket 握手阶段校验 Token 并提取用户信息。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface WebSocketTokenResolver {

    /**
     * 解析 Token
     *
     * @param token 客户端传入的 Token
     * @return Token 中的用户信息，无效 Token 返回 null
     */
    TokenInfo resolve(String token);

    /**
     * Token 解析结果
     */
    @Data
    class TokenInfo {

        private Long userId;

        private Long tenantId;
    }
}
