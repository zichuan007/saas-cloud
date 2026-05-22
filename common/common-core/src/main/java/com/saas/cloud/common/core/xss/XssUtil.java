package com.saas.cloud.common.core.xss;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS 清洗工具
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
public final class XssUtil {

    private static final Safelist SAFE_LIST = Safelist.relaxed()
            .addAttributes(":all", "style", "class");

    private XssUtil() {
    }

    /**
     * 清洗 HTML 内容，移除危险标签和属性
     *
     * @param html 原始文本
     * @return 清洗后的安全文本
     */
    public static String clean(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, "", SAFE_LIST);
    }

    /**
     * 判断文本是否包含 XSS 危险内容
     *
     * @param html 待检测文本
     * @return true-包含危险内容, false-安全
     */
    public static boolean isUnsafe(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }
        return !html.equals(clean(html));
    }
}
