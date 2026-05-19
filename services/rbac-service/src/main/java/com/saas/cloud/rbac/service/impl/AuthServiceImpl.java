package com.saas.cloud.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.exception.ForbiddenException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.common.security.context.UserContext;
import com.saas.cloud.common.security.util.JwtUtils;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.api.dto.RegisterDTO;
import com.saas.cloud.rbac.api.vo.RegisterVO;
import com.saas.cloud.rbac.entity.Dept;
import com.saas.cloud.rbac.entity.Menu;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.entity.RoleMenu;
import com.saas.cloud.rbac.mapper.DeptMapper;
import com.saas.cloud.rbac.mapper.MenuMapper;
import com.saas.cloud.rbac.mapper.RoleMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.mapper.UserRoleMapper;
import com.saas.cloud.rbac.mapper.RoleMenuMapper;
import com.saas.cloud.rbac.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PlatformFeignClient platformFeignClient;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final String REDIS_TOKEN_BLACKLIST = "auth:blacklist:";
    private static final String REDIS_LOGIN_FAIL = "auth:login_fail:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    @Override
    public Map<String, Object> login(String username, String password, String tenantCode) {
        ApiResult<TenantVO> tenantResult = platformFeignClient.getTenantByCode(tenantCode);
        if (tenantResult == null || tenantResult.getData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "租户编码不存在: " + tenantCode);
        }
        Long tenantId = tenantResult.getData().getId();

        checkLoginAttempts(username, tenantId);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getTenantId, tenantId));

        if (user == null || !PASSWORD_ENCODER.matches(password, user.getPassword())) {
            recordLoginFailure(username, tenantId);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
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

        String accessToken = jwtUtils.generateAccessToken(userInfo);
        String refreshToken = jwtUtils.generateRefreshToken(userInfo);

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        clearLoginFailures(username, tenantId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("expiresIn", 7200L);
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
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌无效或已过期");
        }
        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "不是有效的刷新令牌");
        }

        String tokenId = jwtUtils.getTokenId(refreshToken);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_TOKEN_BLACKLIST + tokenId))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "令牌已失效");
        }

        UserContext.UserInfo userInfo = jwtUtils.extractUserInfo(refreshToken);

        Set<String> latestPermissions = loadPermissions(userInfo.getUserId());
        userInfo.setPermissions(latestPermissions);

        String newAccessToken = jwtUtils.generateAccessToken(userInfo);
        String newRefreshToken = jwtUtils.generateRefreshToken(userInfo);

        redisTemplate.opsForValue().set(REDIS_TOKEN_BLACKLIST + tokenId, "1", 7, TimeUnit.DAYS);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("expiresIn", 7200L);
        return result;
    }

    @Override
    public void logout(String token) {
        try {
            String tokenId = jwtUtils.getTokenId(token);
            redisTemplate.opsForValue().set(REDIS_TOKEN_BLACKLIST + tokenId, "1", 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("登出时解析 token 失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 设置租户上下文，确保后续本地数据的 tenant_id 自动填充
        TenantContext.TenantInfo tenantInfo = new TenantContext.TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setTenantName(dto.getTenantName());
        TenantContext.set(tenantInfo);

        try {
            // 2. 创建根部门
            Dept rootDept = new Dept();
            rootDept.setDeptName("总公司");
            rootDept.setParentId(0L);
            rootDept.setAncestors("0");
            rootDept.setLeader(dto.getContactPerson());
            rootDept.setPhone(dto.getPhone());
            rootDept.setSortOrder(0);
            rootDept.setStatus((byte) 1);
            rootDept.setTenantId(tenantId);
            deptMapper.insert(rootDept);
            log.info("创建根部门成功, deptId={}", rootDept.getId());

            // 3. 创建默认角色：租户超管
            Role adminRole = new Role();
            adminRole.setRoleName("租户超管");
            adminRole.setRoleCode("tenant_admin");
            adminRole.setRoleLevel((byte) 0);
            adminRole.setDataScope((byte) 1);
            adminRole.setSortOrder(0);
            adminRole.setStatus((byte) 1);
            adminRole.setIsSystem((byte) 1);
            adminRole.setTenantId(tenantId);
            roleMapper.insert(adminRole);
            log.info("创建默认角色成功, roleId={}", adminRole.getId());

            // 4. 创建管理员用户
            User adminUser = new User();
            adminUser.setUsername(dto.getPhone());
            adminUser.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
            adminUser.setRealName(dto.getContactPerson());
            adminUser.setPhone(dto.getPhone());
            adminUser.setDeptId(rootDept.getId());
            adminUser.setStatus((byte) 1);
            adminUser.setRoleLevel((byte) 0);
            adminUser.setPasswordUpdateTime(LocalDateTime.now());
            adminUser.setTenantId(tenantId);
            userMapper.insert(adminUser);
            log.info("创建管理员用户成功, userId={}", adminUser.getId());

            // 5. 关联用户角色
            UserRole userRole = new UserRole();
            userRole.setUserId(adminUser.getId());
            userRole.setRoleId(adminRole.getId());
            userRole.setTenantId(tenantId);
            userRoleMapper.insert(userRole);
            log.info("关联用户角色成功, userId={}, roleId={}", adminUser.getId(), adminRole.getId());

            // 6. 生成 Token（同登录逻辑）
            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setUserId(adminUser.getId());
            userInfo.setUsername(adminUser.getUsername());
            userInfo.setTenantId(tenantId);
            userInfo.setDeptId(rootDept.getId());
            userInfo.setRoleLevel(0);
            userInfo.setDataScope(1);
            userInfo.setPermissions(Collections.emptySet());

            String accessToken = jwtUtils.generateAccessToken(userInfo);
            String refreshToken = jwtUtils.generateRefreshToken(userInfo);

            // 7. 组装返回结果
            RegisterVO vo = new RegisterVO();
            vo.setTenantId(tenantId);
            vo.setTenantCode(tenantCode);
            vo.setUserId(adminUser.getId());
            vo.setAccessToken(accessToken);
            vo.setRefreshToken(refreshToken);
            vo.setExpiresIn(7200L);

            log.info("租户注册完成, tenantId={}, tenantCode={}, userId={}", tenantId, tenantCode, adminUser.getId());
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
}
