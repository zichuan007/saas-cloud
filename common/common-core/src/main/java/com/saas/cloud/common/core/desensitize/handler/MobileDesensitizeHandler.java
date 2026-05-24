package com.saas.cloud.common.core.desensitize.handler;

import com.saas.cloud.common.core.desensitize.DesensitizeHandler;
import com.saas.cloud.common.core.desensitize.annotation.MobileDesensitize;

/**
 * 手机号脱敏处理器
 * <p>保留前 3 位和后 4 位，中间用 **** 替代。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class MobileDesensitizeHandler implements DesensitizeHandler<MobileDesensitize> {

    @Override
    public String desensitize(String origin, MobileDesensitize annotation) {
        if (origin == null || origin.length() < 7) {
            return origin;
        }
        return origin.substring(0, 3) + "****" + origin.substring(origin.length() - 4);
    }
}
