package com.saas.cloud.common.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class IpRegionUtilsTest {

    @Test
    void shouldResolveKnownIp() {
        String region = IpRegionUtils.getRegion("114.114.114.114");
        assertThat(region).isNotNull();
        assertThat(region).isNotEmpty();
    }

    @Test
    void shouldHandleLocalIp() {
        String region = IpRegionUtils.getRegion("127.0.0.1");
        assertThat(region).isNotNull();
    }

    @Test
    void shouldHandleNullInput() {
        String region = IpRegionUtils.getRegion(null);
        assertThat(region).isNotNull();
    }

    @Test
    void shouldHandleInvalidIp() {
        String region = IpRegionUtils.getRegion("invalid-ip");
        assertThat(region).isNotNull();
    }
}
