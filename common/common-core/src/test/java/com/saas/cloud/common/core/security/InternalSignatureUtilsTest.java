package com.saas.cloud.common.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HMAC 签名工具单测
 *
 * @author saas-cloud
 * @since 2026-07-22
 */
class InternalSignatureUtilsTest {

    private static final String SECRET = "test-secret";

    @Test
    void signAndVerifyRoundTrip() {
        String payload = "GET|/internal/user/123|1718000000000";
        String signature = InternalSignatureUtils.sign(SECRET, payload);
        assertTrue(InternalSignatureUtils.verify(SECRET, payload, signature));
    }

    @Test
    void verifyFailsOnTamperedPayload() {
        String signature = InternalSignatureUtils.sign(SECRET, "payload-A");
        assertFalse(InternalSignatureUtils.verify(SECRET, "payload-B", signature));
    }

    @Test
    void verifyFailsOnWrongSecret() {
        String signature = InternalSignatureUtils.sign(SECRET, "payload");
        assertFalse(InternalSignatureUtils.verify("other-secret", "payload", signature));
    }

    @Test
    void verifyFailsOnEmptySignature() {
        assertFalse(InternalSignatureUtils.verify(SECRET, "payload", null));
        assertFalse(InternalSignatureUtils.verify(SECRET, "payload", ""));
    }

    @Test
    void signIsDeterministic() {
        String s1 = InternalSignatureUtils.sign(SECRET, "payload");
        String s2 = InternalSignatureUtils.sign(SECRET, "payload");
        assertEquals(s1, s2);
    }

    @Test
    void tamperedSignatureRejected() {
        String signature = InternalSignatureUtils.sign(SECRET, "payload");
        String tampered = signature.substring(0, signature.length() - 1)
                + (signature.endsWith("0") ? "1" : "0");
        assertFalse(InternalSignatureUtils.verify(SECRET, "payload", tampered));
    }
}
