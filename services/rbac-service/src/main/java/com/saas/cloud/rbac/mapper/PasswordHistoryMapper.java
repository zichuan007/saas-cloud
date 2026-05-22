package com.saas.cloud.rbac.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.PasswordHistory;

/**
 * 密码历史表 Mapper 接口
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Mapper
public interface PasswordHistoryMapper extends BaseMapper<PasswordHistory> {

}
