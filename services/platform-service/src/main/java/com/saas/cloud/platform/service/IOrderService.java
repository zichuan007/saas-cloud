package com.saas.cloud.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.platform.api.dto.OrderQueryDTO;
import com.saas.cloud.platform.api.dto.OrderSubscribeDTO;
import com.saas.cloud.platform.api.vo.OrderVO;
import com.saas.cloud.platform.entity.Order;

/**
 * 订单服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订购/续费订单
     *
     * @param tenantId 租户ID
     * @param dto      订购请求
     * @return 支付凭证
     */
    String subscribe(Long tenantId, OrderSubscribeDTO dto);

    /**
     * 分页查询订单
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<OrderVO> pageOrders(OrderQueryDTO query);

    /**
     * 手动确认支付（平台端对线下转账订单标记已支付）
     *
     * @param orderId 订单ID
     */
    void confirmPay(Long orderId);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     */
    void cancelOrder(Long orderId);
}
