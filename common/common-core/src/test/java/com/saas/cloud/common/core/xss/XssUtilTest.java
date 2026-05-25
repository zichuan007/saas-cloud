package com.saas.cloud.common.core.xss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class XssUtilTest {

    @Test
    void shouldRemoveScriptTags() {
        String input = "hello<script>alert('xss')</script>world";
        String result = XssUtil.clean(input);
        assertThat(result).doesNotContain("<script>");
        assertThat(result).contains("hello");
        assertThat(result).contains("world");
    }

    @Test
    void shouldRemoveOnEventAttributes() {
        String input = "<div onclick=\"alert('xss')\">click</div>";
        String result = XssUtil.clean(input);
        assertThat(result).doesNotContain("onclick");
    }

    @Test
    void shouldPreserveSafeHtml() {
        String input = "<p>安全内容</p>";
        String result = XssUtil.clean(input);
        assertThat(result).contains("<p>安全内容</p>");
    }

    @Test
    void shouldHandleNullAndEmpty() {
        assertThat(XssUtil.clean(null)).isNull();
        assertThat(XssUtil.clean("")).isEmpty();
    }

    @Test
    void shouldDetectUnsafeContent() {
        assertThat(XssUtil.isUnsafe("<script>alert(1)</script>")).isTrue();
        assertThat(XssUtil.isUnsafe("<p>安全</p>")).isFalse();
        assertThat(XssUtil.isUnsafe(null)).isFalse();
    }

    @Test
    void shouldRemoveImgWithOnerror() {
        String input = "<img src=x onerror=alert(1)>";
        String result = XssUtil.clean(input);
        assertThat(result).doesNotContain("onerror");
    }

    @Test
    void shouldRemoveIframeTags() {
        String input = "<iframe src=\"evil.com\"></iframe>";
        String result = XssUtil.clean(input);
        assertThat(result).doesNotContain("<iframe");
    }
}
