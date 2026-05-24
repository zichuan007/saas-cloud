package com.saas.cloud.common.core.desensitize.handler;

import com.saas.cloud.common.core.desensitize.DesensitizeHandler;
import com.saas.cloud.common.core.desensitize.annotation.BankCardDesensitize;

/**
 * 银行卡号脱敏处理器
 * <p>保留前 4 位和后 4 位，中间用 **** 替代。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class BankCardDesensitizeHandler implements DesensitizeHandler<BankCardDesensitize> {

    @Override
    public String desensitize(String origin, BankCardDesensitize annotation) {
        if (origin == null || origin.length() < 8) {
            return origin;
        }
        return origin.substring(0, 4) + "****" + origin.substring(origin.length() - 4);
    }
}
