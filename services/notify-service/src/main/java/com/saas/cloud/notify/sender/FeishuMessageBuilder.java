package com.saas.cloud.notify.sender;

import org.springframework.stereotype.Component;

/**
 * 飞书消息体构建器
 * <p>构建飞书自定义机器人 Webhook 请求体</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 * @see <a href="https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot">飞书自定义机器人文档</a>
 */
@Component
public class FeishuMessageBuilder {

    /**
     * 构建飞书文本消息 JSON
     *
     * @param title   消息标题
     * @param content 消息内容
     * @return JSON 字符串
     */
    public String buildTextMessage(String title, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"msg_type\":\"interactive\",\"card\":{");
        sb.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"");
        sb.append(escapeJson(title));
        sb.append("\"}},\"elements\":[{\"tag\":\"div\",\"text\":{\"tag\":\"plain_text\",\"content\":\"");
        sb.append(escapeJson(content));
        sb.append("\"}}]}}");
        return sb.toString();
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
