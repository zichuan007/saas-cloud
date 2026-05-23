package com.saas.cloud.notify.sender;

import org.springframework.stereotype.Component;

/**
 * 钉钉消息体构建器
 * <p>构建钉钉自定义机器人 Webhook 请求体</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 * @see <a href="https://open.dingtalk.com/document/orgapp/custom-robots-send-group-messages">钉钉自定义机器人文档</a>
 */
@Component
public class DingtalkMessageBuilder {

    /**
     * 构建钉钉 Markdown 消息 JSON
     *
     * @param title   消息标题
     * @param content 消息内容
     * @return JSON 字符串
     */
    public String buildMarkdownMessage(String title, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"msgtype\":\"markdown\",\"markdown\":{\"title\":\"");
        sb.append(escapeJson(title));
        sb.append("\",\"text\":\"### ");
        sb.append(escapeJson(title));
        sb.append("\\n");
        sb.append(escapeJson(content));
        sb.append("\"}}");
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
