package com.saas.cloud.rbac.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.PostCreateDTO;
import com.saas.cloud.rbac.api.dto.PostUpdateDTO;
import com.saas.cloud.rbac.api.vo.PostVO;
import com.saas.cloud.rbac.entity.Post;
import com.saas.cloud.rbac.entity.UserPost;
import com.saas.cloud.rbac.mapper.PostMapper;
import com.saas.cloud.rbac.mapper.UserPostMapper;
import com.saas.cloud.rbac.service.IPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 岗位 服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private final UserPostMapper userPostMapper;

    @Override
    public PageResult<PostVO> queryPage(String postName, Integer status, int pageNum, int pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(postName != null && !postName.isEmpty(), Post::getPostName, postName)
                .eq(status != null, Post::getStatus, status)
                .orderByAsc(Post::getSortOrder);
        Page<Post> result = baseMapper.selectPage(page, wrapper);
        List<PostVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<PostVO> selectList() {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .orderByAsc(Post::getSortOrder);
        return baseMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PostVO queryById(Long id) {
        Post post = baseMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        return toVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PostCreateDTO createDTO) {
        checkPostCodeUnique(createDTO.getPostCode(), null);
        Post post = new Post();
        BeanUtils.copyProperties(createDTO, post);
        if (post.getSortOrder() == null) {
            post.setSortOrder(0);
        }
        if (post.getStatus() == null) {
            post.setStatus(1);
        }
        baseMapper.insert(post);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PostUpdateDTO updateDTO) {
        Post existing = baseMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("岗位不存在");
        }
        checkPostCodeUnique(updateDTO.getPostCode(), updateDTO.getId());
        BeanUtils.copyProperties(updateDTO, existing);
        baseMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Long userCount = userPostMapper.selectCount(
                new LambdaQueryWrapper<UserPost>().eq(UserPost::getPostId, id));
        if (userCount > 0) {
            throw new BusinessException("该岗位下存在用户，无法删除");
        }
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPosts(Long userId, List<Long> postIds) {
        userPostMapper.delete(
                new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId));

        if (postIds != null && !postIds.isEmpty()) {
            for (Long postId : postIds) {
                UserPost userPost = new UserPost();
                userPost.setUserId(userId);
                userPost.setPostId(postId);
                userPostMapper.insert(userPost);
            }
        }
    }

    @Override
    public List<Long> getPostIdsByUserId(Long userId) {
        List<UserPost> userPosts = userPostMapper.selectList(
                new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userId));
        if (userPosts.isEmpty()) {
            return Collections.emptyList();
        }
        return userPosts.stream()
                .map(UserPost::getPostId)
                .collect(Collectors.toList());
    }

    private void checkPostCodeUnique(String postCode, Long excludeId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getPostCode, postCode);
        if (excludeId != null) {
            wrapper.ne(Post::getId, excludeId);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("岗位编码已存在: " + postCode);
        }
    }

    private PostVO toVO(Post entity) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
