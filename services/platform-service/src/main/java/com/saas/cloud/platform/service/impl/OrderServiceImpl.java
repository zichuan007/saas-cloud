package com.saas.cloud.platform.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.platform.api.dto.OrderQueryDTO;
import com.saas.cloud.platform.api.dto.OrderSubscribeDTO;
import com.saas.cloud.platform.api.vo.OrderVO;
import com.saas.cloud.platform.entity.Order;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.mapper.OrderMapper;
import com.saas.cloud.platform.service.IOrderService;
import com.saas.cloud.platform.service.IPackageService;
import com.saas.cloud.platform.service.ITenantService;
import com.saas.cloud.platform.service.payment.PaymentGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final IPackageService packageService;
    private final ITenantService tenantService;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String subscribe(Long tenantId, OrderSubscribeDTO dto) {
        Package pkg = packageService.getById(dto.getPackageId());
        if (pkg == null) {
            throw new BusinessException("套餐不存在");
        }

        int months = dto.getMonths() != null ? dto.getMonths() : 1;
        BigDecimal amount = pkg.getPriceMonthly().multiply(BigDecimal.valueOf(months));

        String orderNo = generateOrderNo();
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setPackageId(dto.getPackageId());
        order.setOrderNo(orderNo);
        order.setOrderType(dto.getOrderType());
        order.setAmount(amount);
        order.setPayStatus(0);
        order.setPayChannel(dto.getPayChannel());
        this.save(order);

        log.info("[订单] 创建订单: orderNo={}, tenantId={}, packageId={}, amount={}", orderNo, tenantId, dto.getPackageId(), amount);
        return paymentGateway.createPayOrder(orderNo, amount, dto.getPayChannel(), pkg.getPackageName());
    }

    @Override
    public IPage<OrderVO> pageOrders(OrderQueryDTO query) {
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (query.getTenantId() != null) {
            wrapper.eq(Order::getTenantId, query.getTenantId());
        }
        if (query.getPayStatus() != null) {
            wrapper.eq(Order::getPayStatus, query.getPayStatus());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPay(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getPayStatus() != 0) {
            throw new BusinessException("订单状态不允许确认支付");
        }

        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayChannel("manual");
        this.updateById(order);

        activateSubscription(order);
        log.info("[订单] 手动确认支付: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getPayStatus() != 0) {
            throw new BusinessException("仅待支付订单可取消");
        }

        order.setPayStatus(2);
        this.updateById(order);
        log.info("[订单] 取消订单: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    private void activateSubscription(Order order) {
        Tenant tenant = tenantService.getById(order.getTenantId());
        if (tenant == null) {
            return;
        }
        int months = 1;
        Package pkg = packageService.getById(order.getPackageId());
        if (pkg != null && pkg.getPriceMonthly().compareTo(BigDecimal.ZERO) > 0) {
            months = order.getAmount().divide(pkg.getPriceMonthly(), 0, BigDecimal.ROUND_DOWN).intValue();
        }

        LocalDateTime baseTime = tenant.getPaidExpireTime() != null && tenant.getPaidExpireTime().isAfter(LocalDateTime.now())
                ? tenant.getPaidExpireTime() : LocalDateTime.now();
        LocalDateTime expireTime = baseTime.plusMonths(months);

        tenant.setPackageId(order.getPackageId());
        tenant.setPaidExpireTime(expireTime);
        tenant.setStatus(1);
        tenantService.updateById(tenant);

        order.setExpireTime(expireTime);
        this.updateById(order);

        log.info("[订单] 激活订阅: tenantId={}, packageId={}, expireTime={}", order.getTenantId(), order.getPackageId(), expireTime);
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setTenantId(order.getTenantId());
        vo.setPackageId(order.getPackageId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType(order.getOrderType());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayChannel(order.getPayChannel());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setCreateTime(order.getCreateTime());

        Tenant tenant = tenantService.getById(order.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getTenantName());
        }
        Package pkg = packageService.getById(order.getPackageId());
        if (pkg != null) {
            vo.setPackageName(pkg.getPackageName());
        }
        return vo;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
