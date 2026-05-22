package com.saas.cloud.rbac.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.rbac.api.dto.UserCreateDTO;
import com.saas.cloud.rbac.api.dto.UserUpdateDTO;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.api.vo.UserPageVO;
import com.saas.cloud.rbac.entity.Dept;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.mapper.DeptMapper;
import com.saas.cloud.rbac.mapper.RoleMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.mapper.UserRoleMapper;
import com.saas.cloud.rbac.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final PlatformFeignClient platformFeignClient;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public PageResult<UserPageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword) {
        Long tenantId = TenantContext.getTenantId();
        log.info("分页查询用户, tenantId={}, pageNum={}, pageSize={}, keyword={}", tenantId, pageNum, pageSize, keyword);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(tenantId != null, User::getTenantId, tenantId);
        // 关键字模糊搜索：用户名或真实姓名
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(User::getUsername, keyword)
                            .or()
                            .like(User::getRealName, keyword)
            );
        }
        queryWrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = baseMapper.selectPage(page, queryWrapper);

        // 批量查询部门名称
        List<User> userList = userPage.getRecords();
        Map<Long, String> deptNameMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(userList)) {
            List<Long> deptIds = userList.stream()
                    .map(User::getDeptId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(deptIds)) {
                List<Dept> deptList = deptMapper.selectBatchIds(deptIds);
                deptNameMap = deptList.stream()
                        .collect(Collectors.toMap(Dept::getId, Dept::getDeptName, (a, b) -> a));
            }
        }

        // 转换为VO
        Map<Long, String> finalDeptNameMap = deptNameMap;
        List<UserPageVO> voList = userList.stream()
                .map(user -> {
                    UserPageVO vo = new UserPageVO();
                    vo.setUserId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setRealName(user.getRealName());
                    vo.setPhone(user.getPhone());
                    vo.setDeptId(user.getDeptId());
                    vo.setDeptName(finalDeptNameMap.get(user.getDeptId()));
                    vo.setStatus(user.getStatus() != null ? user.getStatus().intValue() : null);
                    vo.setRoleLevel(user.getRoleLevel() != null ? user.getRoleLevel().intValue() : null);
                    vo.setCreateTime(user.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.of(voList, userPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public UserInfoVO getUserDetail(Long userId) {
        log.info("获取用户详情, userId={}", userId);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setTenantId(user.getTenantId());
        vo.setDeptId(user.getDeptId());
        vo.setRoleLevel(user.getRoleLevel() != null ? user.getRoleLevel().intValue() : null);

        // 查询部门名称
        if (user.getDeptId() != null) {
            Dept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (!CollectionUtils.isEmpty(userRoles)) {
            List<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toList());
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            // 可在此设置角色相关信息到VO（当前VO中permissions和menus由其他接口提供）
            log.info("用户 {} 关联角色数: {}", userId, roles.size());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        log.info("创建用户, username={}, tenantId={}", dto.getUsername(), tenantId);

        // 配额校验（非核心逻辑，Feign 调用失败时降级放行）
        if (tenantId != null) {
            try {
                long currentUserCount = this.count(new LambdaQueryWrapper<User>()
                        .eq(User::getTenantId, tenantId));
                ApiResult<Boolean> quotaResult = platformFeignClient.checkQuota(
                        tenantId, "USER", (int) currentUserCount);
                if (quotaResult.isSuccess() && Boolean.FALSE.equals(quotaResult.getData())) {
                    throw new BusinessException(ResultCode.QUOTA_EXCEEDED, "用户数已达套餐上限，请升级套餐");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("用户配额校验异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
            }
        }

        // 检查同租户下用户名唯一
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(tenantId != null, User::getTenantId, tenantId));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus((byte) 1);
        user.setRoleLevel((byte) 2);
        user.setPasswordUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);
        log.info("用户创建成功, id={}", user.getId());

        // 创建用户角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        log.info("更新用户, userId={}", dto.getId());
        User user = baseMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户基本信息
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getDeptId() != null) {
            user.setDeptId(dto.getDeptId());
        }
        baseMapper.updateById(user);

        // 先删后增用户角色关联
        if (dto.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, dto.getId()));
            saveUserRoles(dto.getId(), dto.getRoleIds());
        }
        log.info("用户更新成功, id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        log.info("删除用户, userId={}", userId);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // MyBatis-Plus @TableLogic 自动逻辑删除
        baseMapper.deleteById(userId);
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));
        log.info("用户删除成功, userId={}", userId);
    }

    @Override
    public void updateStatus(Long userId, Byte status) {
        log.info("更新用户状态, userId={}, status={}", userId, status);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        baseMapper.updateById(user);
        log.info("用户状态更新成功, userId={}, status={}", userId, status);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码, userId={}", userId);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
        log.info("用户密码重置成功, userId={}", userId);
    }

    @Override
    public void updateProfile(Long userId, String realName, String phone) {
        log.info("更新个人资料, userId={}", userId);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        baseMapper.updateById(user);
        log.info("个人资料更新成功, userId={}", userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码, userId={}", userId);
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PASSWORD_ENCODER.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
        log.info("密码修改成功, userId={}", userId);
    }

    /**
     * 保存用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        log.info("保存用户角色关联, userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    public List<User> listForExport(String keyword) {
        return this.lambdaQuery()
                .like(org.springframework.util.StringUtils.hasText(keyword), User::getUsername, keyword)
                .or(org.springframework.util.StringUtils.hasText(keyword),
                        w -> w.like(User::getRealName, keyword))
                .orderByDesc(User::getCreateTime)
                .list();
    }
}
