package com.saas.cloud.common.core.desensitize.handler;

import com.saas.cloud.common.core.desensitize.DesensitizeHandler;
import com.saas.cloud.common.core.desensitize.annotation.IdCardDesensitize;

/**
 * 身份证号脱敏处理器
 * <p>保留前 6 位和后 4 位，中间用 ******** 替代。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class IdCardDesensitizeHandler implements DesensitizeHandler<IdCardDesensitize> {

    @Override
    public String desensitize(String origin, IdCardDesensitize annotation) {
        if (origin == null || origin.length() < 10) {
            return origin;
        }
        return origin.substring(0, 6) + "********" + origin.substring(origin.length() - 4);
    }
}
