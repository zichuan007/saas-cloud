package com.saas.cloud.notify.sender;

import java.util.Properties;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件发送器
 * <p>根据渠道配置动态构建 JavaMailSender 并发送邮件</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final ObjectMapper objectMapper;

    /**
     * 发送邮件
     *
     * @param configJson 渠道配置 JSON，格式示例：
     *                   {"host":"smtp.qq.com","port":465,"username":"xxx@qq.com",
     *                   "password":"授权码","from":"xxx@qq.com","ssl":true}
     * @param to         收件人邮箱
     * @param subject    邮件主题
     * @param content    邮件内容
     */
    public void send(String configJson, String to, String subject, String content) {
        try {
            JsonNode config = objectMapper.readTree(configJson);
            JavaMailSender mailSender = buildMailSender(config);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(config.path("from").asText());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("[邮件发送] 发送成功, to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("[邮件发送] 发送失败, to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据配置构建 JavaMailSender
     */
    private JavaMailSender buildMailSender(JsonNode config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.path("host").asText());
        sender.setPort(config.path("port").asInt(465));
        sender.setUsername(config.path("username").asText());
        sender.setPassword(config.path("password").asText());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (config.path("ssl").asBoolean(true)) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");

        return sender;
    }
}
