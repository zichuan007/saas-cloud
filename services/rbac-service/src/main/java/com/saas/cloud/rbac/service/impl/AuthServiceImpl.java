package com.saas.cloud.rbac.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.exception.ForbiddenException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.core.tenant.TenantInitContext;
import com.saas.cloud.common.core.tenant.TenantInitializerRegistry;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.api.dto.RegisterDTO;
import com.saas.cloud.rbac.api.vo.RegisterVO;
import com.saas.cloud.rbac.entity.LoginLog;
import com.saas.cloud.rbac.entity.Menu;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.entity.RoleMenu;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.mapper.DeptMapper;
import com.saas.cloud.rbac.mapper.MenuMapper;
import com.saas.cloud.rbac.mapper.RoleMapper;
import com.saas.cloud.rbac.mapper.RoleMenuMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.mapper.UserRoleMapper;
import com.saas.cloud.rbac.service.IAuthService;
import com.saas.cloud.rbac.service.ILoginLogService;

import com.saas.cloud.common.core.util.IpRegionUtils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthServiceImpl implements IAuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PlatformFeignClient platformFeignClient;
    private final TenantInitializerRegistry tenantInitializerRegistry;
    private final ILoginLogService loginLogService;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final String REDIS_LOGIN_FAIL = "auth:login_fail:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    @TenantIgnore
    @Override
    public Map<String, Object> login(String username, String password, String tenantCode) {
        User user;
        TenantVO tenant;

        if (tenantCode != null && !tenantCode.isEmpty()) {
            // 指定租户编码：原逻辑
            ApiResult<TenantVO> tenantResult = platformFeignClient.getTenantByCode(tenantCode);
            if (tenantResult == null || tenantResult.getData() == null) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "租户编码不存在: " + tenantCode);
            }
            tenant = tenantResult.getData();
            validateTenantForLogin(tenant);

            Long tenantId = tenant.getId();
            checkLoginAttempts(username, tenantId);

            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username)
                    .eq(User::getTenantId, tenantId)
                    .eq(User::getDeleteFlag, 0));
        } else {
            // 未指定租户：按用户名或手机号全局查找
            checkLoginAttemptsByUsername(username);

            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .and(w -> w.eq(User::getUsername, username).or().eq(User::getPhone, username))
                    .eq(User::getDeleteFlag, 0));

            if (users.isEmpty()) {
                recordLoginFailureByUsername(username);
                asyncRecordLoginLog(null, null, username, 0, 0, "用户名或密码错误");
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
            }
            if (users.size() > 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                        "该账号存在于多个企业，请使用手机号登录");
            }
            user = users.get(0);

            ApiResult<TenantVO> tenantResult = platformFeignClient.getTenantInfo(user.getTenantId());
            if (tenantResult == null || tenantResult.getData() == null) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "租户信息异常");
            }
            tenant = tenantResult.getData();
            validateTenantForLogin(tenant);
        }

        Long tenantId = tenant.getId();

        if (user == null || !PASSWORD_ENCODER.matches(password, user.getPassword())) {
            if (tenantCode != null && !tenantCode.isEmpty()) {
                recordLoginFailure(username, tenantId);
            } else {
                recordLoginFailureByUsername(username);
            }
            asyncRecordLoginLog(tenantId, null, username, 0, 0, "用户名或密码错误");
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            asyncRecordLoginLog(tenantId, user.getId(), username, 0, 0, "账号已被禁用");
            throw new ForbiddenException("账号已被禁用");
        }

        Set<String> permissions = loadPermissions(user.getId());

        UserContext.UserInfo userInfo = new UserContext.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setTenantId(tenantId);
        userInfo.setDeptId(user.getDeptId());
        userInfo.setRoleLevel(user.getRoleLevel().intValue());
        userInfo.setDataScope(resolveDataScope(user.getId()));
        userInfo.setPermissions(permissions);

        StpUtil.login(user.getId());
        storeUserInfoToSession(userInfo);
        String tokenValue = StpUtil.getTokenValue();

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        if (tenantCode != null && !tenantCode.isEmpty()) {
            clearLoginFailures(username, tenantId);
        } else {
            clearLoginFailuresByUsername(username);
        }
        asyncRecordLoginLog(tenantId, user.getId(), username, 1, 0, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", tokenValue);
        result.put("refreshToken", tokenValue);
        result.put("expiresIn", StpUtil.getTokenTimeout());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("roleLevel", user.getRoleLevel());
        result.put("permissions", permissions);
        return result;
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        Object loginId = StpUtil.getLoginIdByToken(refreshToken);
        if (loginId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "令牌无效或已过期");
        }

        cn.dev33.satoken.session.SaSession session = StpUtil.getSessionByLoginId(loginId);
        Long userId = session.get("userId", null);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "会话信息已失效");
        }

        Set<String> latestPermissions = loadPermissions(userId);
        session.set("permissions", String.join(",", latestPermissions));

        StpUtil.renewTimeout(StpUtil.getTokenTimeout());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", refreshToken);
        result.put("refreshToken", refreshToken);
        result.put("expiresIn", StpUtil.getTokenTimeout());
        return result;
    }

    @Override
    public void logout(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                StpUtil.logout(loginId);
            }
        } catch (Exception e) {
            log.warn("登出时处理失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @com.saas.cloud.common.redis.lock.DistributedLock(key = "'tenant:register:' + #dto.phone", waitTime = 5, leaseTime = 30)
    public RegisterVO register(RegisterDTO dto) {
        log.info("租户注册开始, tenantName={}, phone={}", dto.getTenantName(), dto.getPhone());

        // 1. 远程调用 platform-service 创建租户
        TenantCreateDTO tenantCreateDTO = new TenantCreateDTO();
        tenantCreateDTO.setTenantName(dto.getTenantName());
        tenantCreateDTO.setContactName(dto.getContactPerson());
        tenantCreateDTO.setContactPhone(dto.getPhone());

        ApiResult<TenantVO> tenantResult = platformFeignClient.createTenant(tenantCreateDTO);
        if (tenantResult == null || tenantResult.getData() == null) {
            throw new BusinessException("创建租户失败，请稍后重试");
        }
        TenantVO tenantVO = tenantResult.getData();
        Long tenantId = tenantVO.getId();
        String tenantCode = tenantVO.getTenantCode();
        log.info("远程创建租户成功, tenantId={}, tenantCode={}", tenantId, tenantCode);

        // 2. 设置租户上下文，确保后续本地数据的 tenant_id 自动填充
        TenantContext.TenantInfo tenantInfo = new TenantContext.TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setTenantName(dto.getTenantName());
        TenantContext.set(tenantInfo);

        try {
            // 3. 通过初始化器链路完成租户初始化（创建部门/角色/用户/角色关联）
            TenantInitContext initContext = TenantInitContext.builder()
                    .tenantId(tenantId)
                    .tenantName(dto.getTenantName())
                    .contactPerson(dto.getContactPerson())
                    .contactPhone(dto.getPhone())
                    .password(PASSWORD_ENCODER.encode(dto.getPassword()))
                    .build();

            tenantInitializerRegistry.initialize(initContext);

            // 4. Sa-Token 登录
            Long adminUserId = initContext.get("adminUserId");
            Long rootDeptId = initContext.get("rootDeptId");

            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setUserId(adminUserId);
            userInfo.setUsername(dto.getPhone());
            userInfo.setTenantId(tenantId);
            userInfo.setDeptId(rootDeptId);
            userInfo.setRoleLevel(0);
            userInfo.setDataScope(1);
            userInfo.setPermissions(Collections.emptySet());

            StpUtil.login(adminUserId);
            storeUserInfoToSession(userInfo);
            String tokenValue = StpUtil.getTokenValue();

            // 5. 组装返回结果
            RegisterVO vo = new RegisterVO();
            vo.setTenantId(tenantId);
            vo.setTenantCode(tenantCode);
            vo.setUserId(adminUserId);
            vo.setAccessToken(tokenValue);
            vo.setRefreshToken(tokenValue);
            vo.setExpiresIn(StpUtil.getTokenTimeout());

            log.info("租户注册完成, tenantId={}, tenantCode={}, userId={}", tenantId, tenantCode, adminUserId);
            return vo;
        } catch (Exception e) {
            log.error("租户注册本地操作失败, tenantId={}, 租户已创建但本地数据回滚", tenantId, e);
            throw new BusinessException("注册失败: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        List<String> roleCodes = loadRoleCodes(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", String.valueOf(user.getId()));
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName() != null ? user.getRealName() : user.getUsername());
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("roles", roleCodes);
        result.put("desc", "");
        result.put("homePath", "/dashboard/analytics");
        result.put("token", "");
        return result;
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        return new ArrayList<>(loadPermissions(userId));
    }

    private List<String> loadRoleCodes(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    private Set<String> loadPermissions(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getRoleLevel() != null && user.getRoleLevel() == 0) {
            List<Menu> allMenus = menuMapper.selectList(
                    new LambdaQueryWrapper<Menu>().eq(Menu::getStatus, (byte) 1));
            return allMenus.stream()
                    .map(Menu::getPermission)
                    .filter(p -> p != null && !p.isEmpty())
                    .collect(Collectors.toSet());
        }

        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getRoleId, roleIds));
        if (roleMenus.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        List<Menu> menus = menuMapper.selectBatchIds(menuIds);
        return menus.stream()
                .map(Menu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toSet());
    }

    private Integer resolveDataScope(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return 4;
        }
        // TODO: 从 sys_role 查 data_scope，取最大权限（最小值）
        return 4;
    }

    private void checkLoginAttempts(String username, Long tenantId) {
        String key = REDIS_LOGIN_FAIL + tenantId + ":" + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            asyncRecordLoginLog(tenantId, null, username, 0, 0,
                    "登录失败次数过多，账号已锁定" + LOCK_MINUTES + "分钟");
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(),
                    "登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试");
        }
    }

    private void recordLoginFailure(String username, Long tenantId) {
        String key = REDIS_LOGIN_FAIL + tenantId + ":" + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void clearLoginFailures(String username, Long tenantId) {
        redisTemplate.delete(REDIS_LOGIN_FAIL + tenantId + ":" + username);
    }

    private void checkLoginAttemptsByUsername(String username) {
        String key = REDIS_LOGIN_FAIL + "global:" + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(),
                    "登录失败次数过多，请" + LOCK_MINUTES + "分钟后再试");
        }
    }

    private void recordLoginFailureByUsername(String username) {
        String key = REDIS_LOGIN_FAIL + "global:" + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void clearLoginFailuresByUsername(String username) {
        redisTemplate.delete(REDIS_LOGIN_FAIL + "global:" + username);
    }

    /**
     * 将用户信息存入 Sa-Token Session（使用独立 key，避免跨服务反序列化耦合）
     */
    private void storeUserInfoToSession(UserContext.UserInfo userInfo) {
        cn.dev33.satoken.session.SaSession session = StpUtil.getSession();
        session.set("userId", userInfo.getUserId());
        session.set("username", userInfo.getUsername());
        session.set("tenantId", userInfo.getTenantId());
        session.set("deptId", userInfo.getDeptId());
        session.set("roleLevel", userInfo.getRoleLevel());
        session.set("dataScope", userInfo.getDataScope());
        session.set("permissions", userInfo.getPermissions() != null ? String.join(",", userInfo.getPermissions()) : "");
    }

    /**
     * 异步记录登录日志
     */
    private void asyncRecordLoginLog(Long tenantId, Long userId, String username,
                                     int status, int loginType, String errorMsg) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setTenantId(tenantId);
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setStatus(status);
            loginLog.setLoginType(loginType);
            loginLog.setErrorMsg(errorMsg);
            loginLog.setLoginTime(LocalDateTime.now());

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = getClientIp(request);
                String uaStr = request.getHeader("User-Agent");
                loginLog.setIp(ip);
                loginLog.setLocation(IpRegionUtils.getRegion(ip));
                loginLog.setUserAgent(uaStr);
                if (uaStr != null) {
                    UserAgent ua = UserAgentUtil.parse(uaStr);
                    loginLog.setBrowser(ua.getBrowser().getName() + " " + ua.getVersion());
                    loginLog.setOs(ua.getOs().getName());
                }
            }
            loginLogService.recordLoginLog(loginLog);
        } catch (Exception e) {
            log.warn("构建登录日志失败: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 校验租户状态是否允许登录
     */
    private void validateTenantForLogin(TenantVO tenant) {
        if (Objects.equals(tenant.getStatus(), TenantStatusEnum.FROZEN.getCode())) {
            throw new ForbiddenException("企业已被冻结，请联系管理员");
        }
        if (Objects.equals(tenant.getStatus(), TenantStatusEnum.DEACTIVATED.getCode())) {
            throw new ForbiddenException("企业已注销");
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("企业已过期，请联系管理员续费");
        }
    }
}
