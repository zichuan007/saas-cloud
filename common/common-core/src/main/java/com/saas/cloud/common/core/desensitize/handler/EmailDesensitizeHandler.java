package com.saas.cloud.common.core.desensitize.handler;

import com.saas.cloud.common.core.desensitize.DesensitizeHandler;
import com.saas.cloud.common.core.desensitize.annotation.EmailDesensitize;

/**
 * 邮箱脱敏处理器
 * <p>邮箱前缀保留前 2 个字符，其余用 **** 替代，@ 及域名保留。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class EmailDesensitizeHandler implements DesensitizeHandler<EmailDesensitize> {

    @Override
    public String desensitize(String origin, EmailDesensitize annotation) {
        if (origin == null) {
            return null;
        }
        int atIndex = origin.indexOf('@');
        if (atIndex <= 2) {
            return origin;
        }
        return origin.substring(0, 2) + "****" + origin.substring(atIndex);
    }
}
