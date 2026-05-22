package com.saas.cloud.rbac.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.Menu;

/**
 * 菜单表 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户ID查询有权限的菜单（仅目录和菜单，不含按钮）
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);
}
