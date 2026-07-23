package com.saas.cloud.common.core.security;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内部调用签名服务
 * <p>提供网关→下游用户身份头签名/验签、服务间内部调用签名/验签。
 * 采用影子模式：{@code enforced=false} 时仅记录告警不拦截，
 * 待运行验证签名链路正常后置 {@code saas.security.signature-enforced=true} 启用强制校验。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-07-22
 */
public class InternalSignatureService {

    private final String secret;
    private final boolean enforced;
    private final long maxAgeSeconds;

    public InternalSignatureService(String secret, boolean enforced, long maxAgeSeconds) {
        this.secret = secret;
        this.enforced = enforced;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public boolean isEnforced() {
        return enforced;
    }

    /**
     * 签名网关透传的用户身份头（按头名排序拼接 + 时间戳）
     */
    public String signUserHeaders(Map<String, String> headers, long timestamp) {
        return InternalSignatureUtils.sign(secret, canonical(headers) + "|" + timestamp);
    }

    public VerifyResult verifyUserHeaders(Map<String, String> headers, String signature, Long timestamp) {
        if (timestamp == null) {
            return VerifyResult.fail("缺少时间戳");
        }
        if (Math.abs(System.currentTimeMillis() - timestamp) > maxAgeSeconds * 1000L) {
            return VerifyResult.fail("时间戳过期");
        }
        boolean ok = InternalSignatureUtils.verify(secret, canonical(headers) + "|" + timestamp, signature);
        return ok ? VerifyResult.ok() : VerifyResult.fail("签名不匹配");
    }

    /**
     * 签名服务间内部调用（method + path + 时间戳）
     */
    public String signInternal(String method, String path, long timestamp) {
        return InternalSignatureUtils.sign(secret, method + "|" + path + "|" + timestamp);
    }

    public VerifyResult verifyInternal(String method, String path, String signature, Long timestamp) {
        if (timestamp == null) {
            return VerifyResult.fail("缺少时间戳");
        }
        if (Math.abs(System.currentTimeMillis() - timestamp) > maxAgeSeconds * 1000L) {
            return VerifyResult.fail("时间戳过期");
        }
        boolean ok = InternalSignatureUtils.verify(secret, method + "|" + path + "|" + timestamp, signature);
        return ok ? VerifyResult.ok() : VerifyResult.fail("签名不匹配");
    }

    private String canonical(Map<String, String> headers) {
        return headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * 验签结果
     */
    public static class VerifyResult {
        private final boolean ok;
        private final String reason;

        private VerifyResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        public static VerifyResult ok() {
            return new VerifyResult(true, null);
        }

        public static VerifyResult fail(String reason) {
            return new VerifyResult(false, reason);
        }

        public boolean isOk() {
            return ok;
        }

        public String getReason() {
            return reason;
        }
    }
}
