package com.saas.cloud.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.platform.entity.Order;

import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-25
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
