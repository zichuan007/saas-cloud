package com.saas.cloud.rbac.service.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.config.SocialLoginConfig;
import com.saas.cloud.rbac.entity.SocialUser;
import com.saas.cloud.rbac.entity.User;
import com.saas.cloud.rbac.mapper.SocialUserMapper;
import com.saas.cloud.rbac.mapper.UserMapper;
import com.saas.cloud.rbac.service.ISocialLoginService;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDingTalkRequest;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.utils.AuthStateUtils;

/**
 * 社交登录服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SocialLoginServiceImpl implements ISocialLoginService {

    private final SocialLoginConfig socialLoginConfig;
    private final SocialUserMapper socialUserMapper;
    private final UserMapper userMapper;
    private final PlatformFeignClient platformFeignClient;

    @Override
    public String getAuthorizeUrl(String type, String tenantCode) {
        AuthRequest authRequest = buildAuthRequest(type);
        return authRequest.authorize(AuthStateUtils.createState() + "|" + tenantCode);
    }

    @TenantIgnore
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> callback(String type, String tenantCode, String code, String state) {
        // 解析租户
        ApiResult<TenantVO> tenantResult = platformFeignClient.getTenantByCode(tenantCode);
        if (tenantResult == null || tenantResult.getData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "租户编码不存在: " + tenantCode);
        }
        Long tenantId = tenantResult.getData().getId();

        // 执行第三方登录回调
        AuthRequest authRequest = buildAuthRequest(type);
        AuthCallback authCallback = AuthCallback.builder().code(code).state(state).build();
        AuthResponse<AuthUser> response = authRequest.login(authCallback);

        if (!response.ok()) {
            log.error("[社交登录] 回调失败, type={}, msg={}", type, response.getMsg());
            throw new BusinessException("第三方登录失败: " + response.getMsg());
        }

        AuthUser authUser = response.getData();
        String socialId = authUser.getUuid();

        // 查找是否已绑定
        SocialUser socialUser = socialUserMapper.selectOne(
                new LambdaQueryWrapper<SocialUser>()
                        .eq(SocialUser::getTenantId, tenantId)
                        .eq(SocialUser::getSocialType, type)
                        .eq(SocialUser::getSocialId, socialId));

        Map<String, Object> result = new LinkedHashMap<>();
        if (socialUser != null) {
            // 已绑定 → 直接登录
            User user = userMapper.selectById(socialUser.getUserId());
            if (user == null || user.getStatus() == 0) {
                throw new BusinessException("关联的系统用户不存在或已禁用");
            }
            StpUtil.login(user.getId());
            result.put("bound", true);
            result.put("accessToken", StpUtil.getTokenValue());
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
        } else {
            // 未绑定 → 返回第三方信息供前端绑定
            result.put("bound", false);
            result.put("socialType", type);
            result.put("socialId", socialId);
            result.put("socialName", authUser.getNickname());
            result.put("socialAvatar", authUser.getAvatar());
        }
        return result;
    }

    @TenantIgnore
    @Override
    public void bindSocial(Long userId, String socialType, String socialId, String socialName, String socialAvatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否已被其他用户绑定
        SocialUser existing = socialUserMapper.selectOne(
                new LambdaQueryWrapper<SocialUser>()
                        .eq(SocialUser::getTenantId, user.getTenantId())
                        .eq(SocialUser::getSocialType, socialType)
                        .eq(SocialUser::getSocialId, socialId));
        if (existing != null) {
            throw new BusinessException("该社交账号已被其他用户绑定");
        }

        SocialUser socialUser = new SocialUser();
        socialUser.setTenantId(user.getTenantId());
        socialUser.setUserId(userId);
        socialUser.setSocialType(socialType);
        socialUser.setSocialId(socialId);
        socialUser.setSocialName(socialName);
        socialUser.setSocialAvatar(socialAvatar);
        socialUserMapper.insert(socialUser);
        log.info("[社交登录] 绑定成功, userId={}, socialType={}, socialId={}", userId, socialType, socialId);
    }

    @TenantIgnore
    @Override
    public void unbindSocial(Long userId, String socialType) {
        socialUserMapper.delete(new LambdaQueryWrapper<SocialUser>()
                .eq(SocialUser::getUserId, userId)
                .eq(SocialUser::getSocialType, socialType));
        log.info("[社交登录] 解绑成功, userId={}, socialType={}", userId, socialType);
    }

    @TenantIgnore
    @Override
    public List<SocialUser> listBoundSocials(Long userId) {
        return socialUserMapper.selectList(
                new LambdaQueryWrapper<SocialUser>()
                        .eq(SocialUser::getUserId, userId)
                        .select(SocialUser::getSocialType, SocialUser::getSocialName,
                                SocialUser::getSocialAvatar, SocialUser::getCreateTime));
    }

    /**
     * 根据平台类型构建 JustAuth AuthRequest
     */
    private AuthRequest buildAuthRequest(String type) {
        SocialLoginConfig.PlatformConfig config = socialLoginConfig.getPlatforms().get(type);
        if (config == null) {
            throw new BusinessException("不支持的社交登录类型: " + type);
        }
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .redirectUri(config.getRedirectUri())
                .build();

        switch (type) {
            case "wechat":
                return new AuthWeChatOpenRequest(authConfig);
            case "dingtalk":
                return new AuthDingTalkRequest(authConfig);
            case "github":
                return new AuthGithubRequest(authConfig);
            case "gitee":
                return new AuthGiteeRequest(authConfig);
            default:
                throw new BusinessException("不支持的社交登录类型: " + type);
        }
    }
}
