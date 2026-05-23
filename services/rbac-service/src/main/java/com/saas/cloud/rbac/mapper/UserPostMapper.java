package com.saas.cloud.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.UserPost;

import org.apache.ibatis.annotations.Mapper;

/**
 * 用户岗位关联表 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPost> {

}
