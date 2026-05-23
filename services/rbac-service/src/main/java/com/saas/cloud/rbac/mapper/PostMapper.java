package com.saas.cloud.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.Post;

import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位表 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

}
