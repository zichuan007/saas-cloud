package com.saas.cloud.common.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DFA（确定有限自动机）敏感词过滤器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public class DfaFilter {

    private static final String IS_END_KEY = "isEnd";

    private Map<Character, Object> rootMap = new HashMap<>();

    /**
     * 初始化敏感词库
     *
     * @param words 敏感词集合
     */
    @SuppressWarnings("unchecked")
    public void init(Collection<String> words) {
        Map<Character, Object> newRoot = new HashMap<>();
        for (String word : words) {
            if (word == null || word.isBlank()) {
                continue;
            }
            Map<Character, Object> current = newRoot;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Map<Character, Object> child = (Map<Character, Object>) current.get(c);
                if (child == null) {
                    child = new HashMap<>();
                    current.put(c, child);
                }
                current = child;
            }
            current.put(IS_END_KEY.charAt(0), null);
        }
        this.rootMap = newRoot;
    }

    /**
     * 检测文本中是否包含敏感词
     *
     * @param text 待检测文本
     * @return 命中的敏感词列表
     */
    @SuppressWarnings("unchecked")
    public List<String> check(String text) {
        List<String> found = new ArrayList<>();
        if (text == null || text.isBlank() || rootMap.isEmpty()) {
            return found;
        }
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> current = rootMap;
            int j = i;
            while (j < text.length()) {
                char c = text.charAt(j);
                current = (Map<Character, Object>) current.get(c);
                if (current == null) {
                    break;
                }
                if (current.containsKey(IS_END_KEY.charAt(0))) {
                    found.add(text.substring(i, j + 1));
                }
                j++;
            }
        }
        return found;
    }

    /**
     * 过滤文本，将敏感词替换为指定字符
     *
     * @param text        待过滤文本
     * @param replacement 替换字符
     * @return 过滤后的文本
     */
    @SuppressWarnings("unchecked")
    public String filter(String text, char replacement) {
        if (text == null || text.isBlank() || rootMap.isEmpty()) {
            return text;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Map<Character, Object> current = rootMap;
            int j = i;
            int matchEnd = -1;
            while (j < chars.length) {
                char c = chars[j];
                current = (Map<Character, Object>) current.get(c);
                if (current == null) {
                    break;
                }
                if (current.containsKey(IS_END_KEY.charAt(0))) {
                    matchEnd = j;
                }
                j++;
            }
            if (matchEnd >= i) {
                for (int k = i; k <= matchEnd; k++) {
                    chars[k] = replacement;
                }
                i = matchEnd;
            }
        }
        return new String(chars);
    }
}
