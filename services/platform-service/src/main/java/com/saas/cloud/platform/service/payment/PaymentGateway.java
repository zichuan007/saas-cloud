package com.saas.cloud.platform.service.payment;

import java.math.BigDecimal;

/**
 * 支付网关接口
 * <p>后续接入支付宝/微信只需实现此接口并替换 Bean。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
public interface PaymentGateway {

    /**
     * 创建支付订单
     *
     * @param orderNo    订单号
     * @param amount     金额
     * @param channel    支付渠道
     * @param subject    订单标题
     * @return 支付凭证（支付链接/二维码内容等），Mock 实现返回固定字符串
     */
    String createPayOrder(String orderNo, BigDecimal amount, String channel, String subject);
}
