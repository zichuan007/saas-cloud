package com.saas.cloud.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.PostCreateDTO;
import com.saas.cloud.rbac.api.dto.PostUpdateDTO;
import com.saas.cloud.rbac.api.vo.PostVO;
import com.saas.cloud.rbac.entity.Post;

import java.util.List;

/**
 * 岗位 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface IPostService extends IService<Post> {

    /**
     * 分页查询岗位列表
     *
     * @param postName 岗位名称筛选
     * @param status   状态筛选
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<PostVO> queryPage(String postName, Integer status, int pageNum, int pageSize);

    /**
     * 下拉选择（全量返回启用岗位）
     *
     * @return 岗位列表
     */
    List<PostVO> selectList();

    /**
     * 根据 ID 查询详情
     *
     * @param id 岗位ID
     * @return 详情
     */
    PostVO queryById(Long id);

    /**
     * 创建岗位
     *
     * @param createDTO 创建参数
     * @return 主键ID
     */
    Long create(PostCreateDTO createDTO);

    /**
     * 更新岗位
     *
     * @param updateDTO 修改参数
     */
    void update(PostUpdateDTO updateDTO);

    /**
     * 删除岗位
     *
     * @param id 岗位ID
     */
    void deleteById(Long id);

    /**
     * 保存用户岗位关联
     *
     * @param userId  用户ID
     * @param postIds 岗位ID列表
     */
    void saveUserPosts(Long userId, List<Long> postIds);

    /**
     * 查询用户关联的岗位ID列表
     *
     * @param userId 用户ID
     * @return 岗位ID列表
     */
    List<Long> getPostIdsByUserId(Long userId);
}
