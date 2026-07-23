package com.saas.cloud.common.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 内部调用 HMAC-SHA256 签名工具
 * <p>用于网关→下游用户身份头签名、服务间内部调用签名校验。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-07-22
 */
public final class InternalSignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private InternalSignatureUtils() {
    }

    /**
     * 计算 HMAC-SHA256 签名（十六进制小写）
     *
     * @param secret  共享密钥
     * @param payload 待签名串
     * @return 签名 hex
     */
    public static String sign(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 签名计算失败", e);
        }
    }

    /**
     * 常量时间比较验签，避免时序攻击
     */
    public static boolean verify(String secret, String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        String expected = sign(secret, payload);
        return constantTimeEquals(expected, signature);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }

    /**
     * SHA-256（供可选的轻量校验场景，当前未使用）
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }
}
