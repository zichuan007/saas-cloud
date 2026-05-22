package com.saas.cloud.rbac.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.cloud.rbac.entity.Dept;

/**
 * 部门表 Mapper 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 批量更新子部门的 ancestors，将旧前缀替换为新前缀
     *
     * @param oldAncestors 旧的祖先链
     * @param newAncestors 新的祖先链
     */
    void updateChildAncestors(@Param("oldAncestors") String oldAncestors,
                              @Param("newAncestors") String newAncestors);
}
