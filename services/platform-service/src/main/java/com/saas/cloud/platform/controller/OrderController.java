package com.saas.cloud.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.dto.OrderQueryDTO;
import com.saas.cloud.platform.api.dto.OrderSubscribeDTO;
import com.saas.cloud.platform.api.vo.OrderVO;
import com.saas.cloud.platform.service.IOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 订单管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderController {

    private final IOrderService orderService;

    /**
     * 订购/续费
     *
     * @param tenantId 租户ID（租户端从上下文获取，平台端手动指定）
     * @param dto      订购请求
     * @return 支付凭证
     */
    @Operation(summary = "创建订购/续费订单")
    @OperationLog(module = "订单管理", operation = "创建订单")
    @PostMapping("/subscribe/{tenantId}")
    public ApiResult<String> subscribe(@PathVariable("tenantId") Long tenantId,
                                       @Validated @RequestBody OrderSubscribeDTO dto) {
        return ApiResult.ok(orderService.subscribe(tenantId, dto));
    }

    /**
     * 分页查询订单
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public ApiResult<IPage<OrderVO>> pageOrders(OrderQueryDTO query) {
        return ApiResult.ok(orderService.pageOrders(query));
    }

    /**
     * 手动确认支付（平台端）
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @Operation(summary = "手动确认支付")
    @OperationLog(module = "订单管理", operation = "确认支付")
    @PutMapping("/{id}/confirm-pay")
    public ApiResult<Void> confirmPay(@PathVariable("id") Long id) {
        orderService.confirmPay(id);
        return ApiResult.ok();
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
    @OperationLog(module = "订单管理", operation = "取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancelOrder(@PathVariable("id") Long id) {
        orderService.cancelOrder(id);
        return ApiResult.ok();
    }
}
