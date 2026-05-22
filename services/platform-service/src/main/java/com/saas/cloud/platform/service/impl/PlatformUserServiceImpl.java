package com.saas.cloud.platform.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.platform.api.vo.PlatformUserVO;
import com.saas.cloud.platform.entity.PlatformUser;
import com.saas.cloud.platform.mapper.PlatformUserMapper;
import com.saas.cloud.platform.service.IPlatformUserService;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 平台用户服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser> implements IPlatformUserService {

    /** 平台管理员的租户ID固定为0 */
    private static final long PLATFORM_TENANT_ID = 0L;
    /** 超级管理员角色级别 */
    private static final int SUPER_ADMIN_ROLE_LEVEL = -1;
    /** 平台权限标识 */
    private static final String PLATFORM_ALL_PERMISSION = "platform:*";
    /** 超管角色类型 */
    private static final byte ROLE_TYPE_SUPER_ADMIN = 0;
    /** 运营角色类型 */
    private static final byte ROLE_TYPE_OPERATOR = 1;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> platformLogin(String username, String password) {
        // 根据用户名查询平台用户
        PlatformUser user = this.lambdaQuery()
                .eq(PlatformUser::getUsername, username)
                .one();

        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "账号已禁用");
        }

        // BCrypt 密码校验
        if (!PASSWORD_ENCODER.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 构造用户信息，设置平台级权限
        Set<String> permissions = new HashSet<>();
        permissions.add(PLATFORM_ALL_PERMISSION);

        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setTenantId(PLATFORM_TENANT_ID);
        userInfo.setDeptId(0L);
        userInfo.setRoleLevel(SUPER_ADMIN_ROLE_LEVEL);
        userInfo.setDataScope(0);
        userInfo.setPermissions(permissions);

        StpUtil.login(user.getId());
        cn.dev33.satoken.session.SaSession session = StpUtil.getSession();
        session.set("userId", userInfo.getUserId());
        session.set("username", userInfo.getUsername());
        session.set("tenantId", userInfo.getTenantId());
        session.set("deptId", userInfo.getDeptId());
        session.set("roleLevel", userInfo.getRoleLevel());
        session.set("dataScope", userInfo.getDataScope());
        session.set("permissions", userInfo.getPermissions() != null ? String.join(",", userInfo.getPermissions()) : "");
        String tokenValue = StpUtil.getTokenValue();

        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        log.info("平台管理员登录成功, userId={}, username={}", user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>(8);
        result.put("accessToken", tokenValue);
        result.put("refreshToken", tokenValue);
        result.put("expiresIn", StpUtil.getTokenTimeout());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("roleType", user.getRoleType());
        return result;
    }

    @Override
    public PlatformUserVO getCurrentUser(Long userId) {
        PlatformUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在, userId=" + userId);
        }

        PlatformUserVO vo = new PlatformUserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRoleType(user.getRoleType());
        vo.setRoleTypeDesc(getRoleTypeDesc(user.getRoleType()));
        return vo;
    }

    /**
     * 获取角色类型描述
     *
     * @param roleType 角色类型
     * @return 角色类型描述
     */
    private String getRoleTypeDesc(Byte roleType) {
        if (roleType == null) {
            return "未知";
        }
        if (roleType == ROLE_TYPE_SUPER_ADMIN) {
            return "超级管理员";
        }
        if (roleType == ROLE_TYPE_OPERATOR) {
            return "运营人员";
        }
        return "未知";
    }
}
