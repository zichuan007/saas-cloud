package com.saas.cloud.platform.service.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock 支付网关实现
 * <p>直接返回模拟支付成功凭证，用于开发和测试环境。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String createPayOrder(String orderNo, BigDecimal amount, String channel, String subject) {
        log.info("[MockPayment] 创建模拟支付订单: orderNo={}, amount={}, channel={}, subject={}", orderNo, amount, channel, subject);
        return "MOCK_PAY_" + orderNo;
    }
}
