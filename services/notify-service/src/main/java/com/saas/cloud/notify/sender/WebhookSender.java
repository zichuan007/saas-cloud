package com.saas.cloud.notify.sender;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用 Webhook 发送器
 * <p>向飞书/钉钉/企业微信等平台的 Webhook 地址发送 JSON 消息</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Component
public class WebhookSender {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送 Webhook 消息
     *
     * @param webhookUrl Webhook 地址
     * @param jsonBody   JSON 消息体
     * @param platform   平台名称（用于日志标识）
     */
    public void send(String webhookUrl, String jsonBody, String platform) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("[Webhook-{}] 发送成功, status={}, body={}", platform, response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            log.error("[Webhook-{}] 发送失败, url={}", platform, webhookUrl, e);
            throw new RuntimeException(platform + " Webhook发送失败: " + e.getMessage(), e);
        }
    }
}
