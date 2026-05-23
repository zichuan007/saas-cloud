package com.saas.cloud.rbac.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.rbac.api.vo.OnlineUserVO;
import com.saas.cloud.rbac.service.IOnlineUserService;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 在线用户 服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OnlineUserServiceImpl implements IOnlineUserService {

    @Override
    public List<OnlineUserVO> listOnlineUsers(String username) {
        Long currentTenantId = TenantContext.getTenantId();
        List<OnlineUserVO> result = new ArrayList<>();

        List<String> tokenValues = StpUtil.searchTokenValue("", 0, -1, true);
        for (String tokenValue : tokenValues) {
            try {
                String token = tokenValue.replace(StpUtil.stpLogic.getConfigOrGlobal().getTokenName() + ":", "");
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    continue;
                }

                SaSession session = StpUtil.getSessionByLoginId(loginId, false);
                if (session == null) {
                    continue;
                }

                Long tenantId = session.get("tenantId", null);
                if (!Objects.equals(tenantId, currentTenantId)) {
                    continue;
                }

                String sessionUsername = session.get("username", "");
                if (username != null && !username.isEmpty()
                        && !sessionUsername.contains(username)) {
                    continue;
                }

                OnlineUserVO vo = new OnlineUserVO();
                vo.setUserId(session.get("userId", null));
                vo.setUsername(sessionUsername);
                vo.setTenantId(tenantId);
                vo.setDeptId(session.get("deptId", null));
                vo.setTokenValue(maskToken(token));
                result.add(vo);
            } catch (Exception e) {
                log.debug("解析在线用户信息异常: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    public void kickout(String tokenValue) {
        Object loginId = StpUtil.getLoginIdByToken(tokenValue);
        if (loginId != null) {
            StpUtil.kickout(loginId);
            log.info("强制下线用户: loginId={}", loginId);
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return token;
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
