package com.saas.cloud.common.log.diff;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 对象字段差异对比工具
 * <p>对比两个同类型对象中标注了 {@link DiffField} 的字段值差异，
 * 生成人类可读的变更描述文本。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public final class DiffUtil {

    /** Diff 结果最大长度，超过则截断 */
    private static final int MAX_DIFF_LENGTH = 2000;

    private DiffUtil() {
    }

    /**
     * 对比两个对象的差异
     *
     * @param before 修改前的对象
     * @param after  修改后的对象
     * @return 差异描述文本，格式如 "名称: 张三 → 李四; 状态: 启用 → 禁用"，无差异返回 null
     */
    public static String diff(Object before, Object after) {
        if (before == null || after == null) {
            return null;
        }
        if (!before.getClass().equals(after.getClass())) {
            log.warn("[DiffUtil] 对比对象类型不一致: {} vs {}", before.getClass(), after.getClass());
            return null;
        }

        List<String> changes = new ArrayList<>();
        collectDiffs(before, after, before.getClass(), changes);

        if (changes.isEmpty()) {
            return null;
        }

        String result = String.join("; ", changes);
        if (result.length() > MAX_DIFF_LENGTH) {
            return result.substring(0, MAX_DIFF_LENGTH) + "...(truncated)";
        }
        return result;
    }

    /**
     * 递归收集当前类及父类中标注了 @DiffField 的字段差异
     */
    private static void collectDiffs(Object before, Object after, Class<?> clazz, List<String> changes) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        // 先处理父类
        collectDiffs(before, after, clazz.getSuperclass(), changes);

        for (Field field : clazz.getDeclaredFields()) {
            DiffField diffField = field.getAnnotation(DiffField.class);
            if (diffField == null) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object oldVal = field.get(before);
                Object newVal = field.get(after);

                if (!Objects.equals(oldVal, newVal)) {
                    String label = diffField.value();
                    changes.add(label + ": " + formatValue(oldVal) + " → " + formatValue(newVal));
                }
            } catch (IllegalAccessException e) {
                log.warn("[DiffUtil] 字段访问失败: {}.{}", clazz.getSimpleName(), field.getName(), e);
            }
        }
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "空";
        }
        String str = String.valueOf(value);
        if (str.length() > 100) {
            return str.substring(0, 100) + "...";
        }
        return str;
    }
}
