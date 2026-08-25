package com.saas.cloud.common.websocket.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.websocket.handler.JsonWebSocketHandler;
import com.saas.cloud.common.websocket.handler.WebSocketMessageListener;
import com.saas.cloud.common.websocket.interceptor.LoginUserHandshakeInterceptor;
import com.saas.cloud.common.websocket.interceptor.WebSocketTokenResolver;
import com.saas.cloud.common.websocket.sender.LocalWebSocketMessageSender;
import com.saas.cloud.common.websocket.sender.MqWebSocketMessageSender;
import com.saas.cloud.common.websocket.sender.WebSocketMessageSender;
import com.saas.cloud.common.websocket.session.WebSocketSessionManager;

/**
 * WebSocket 自动配置
 * <p>当应用提供了 {@link WebSocketTokenResolver} Bean 时自动激活。
 * 有 Kafka 时使用集群广播实现，否则使用本地发送。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Configuration
@EnableWebSocket
@ConditionalOnBean(WebSocketTokenResolver.class)
public class WebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebSocketSessionManager webSocketSessionManager() {
        return new WebSocketSessionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonWebSocketHandler jsonWebSocketHandler(WebSocketSessionManager sessionManager,
                                                      ObjectProvider<WebSocketMessageListener> listenerProvider) {
        return new JsonWebSocketHandler(sessionManager, listenerProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginUserHandshakeInterceptor loginUserHandshakeInterceptor(WebSocketTokenResolver tokenResolver) {
        return new LoginUserHandshakeInterceptor(tokenResolver);
    }

    @Bean
    public WebSocketConfigurer webSocketConfigurer(JsonWebSocketHandler handler,
                                                    LoginUserHandshakeInterceptor interceptor) {
        return registry -> registry.addHandler(handler, "/ws")
                .addInterceptors(interceptor)
                .setAllowedOriginPatterns("*");
    }

    /**
     * MQ 集群广播模式
     */
    @Configuration
    @ConditionalOnBean(MessageSender.class)
    static class MqSenderConfig {

        @Bean
        @ConditionalOnMissingBean(WebSocketMessageSender.class)
        public MqWebSocketMessageSender mqWebSocketMessageSender(
                MessageSender messageSender,
                WebSocketSessionManager sessionManager,
                ObjectMapper objectMapper) {
            return new MqWebSocketMessageSender(messageSender, sessionManager, objectMapper);
        }
    }

    /**
     * 本地发送模式（无 MQ 时降级）
     */
    @Configuration
    static class LocalSenderConfig {

        @Bean
        @ConditionalOnMissingBean(WebSocketMessageSender.class)
        public LocalWebSocketMessageSender localWebSocketMessageSender(WebSocketSessionManager sessionManager) {
            return new LocalWebSocketMessageSender(sessionManager);
        }
    }
}
