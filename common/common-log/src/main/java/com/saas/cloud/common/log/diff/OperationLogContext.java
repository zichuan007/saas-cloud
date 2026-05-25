package com.saas.cloud.common.log.diff;

/**
 * 操作日志上下文（ThreadLocal）
 * <p>供 Service 层在更新操作中设置修改前后的对象，
 * {@link com.saas.cloud.common.log.aspect.OperationLogAspect} 会在切面中读取并计算 Diff。</p>
 *
 * <pre>
 * 使用示例（Service 层）：
 *   User oldUser = userMapper.selectById(id);
 *   OperationLogContext.setBeforeData(oldUser);
 *   // ... 执行更新
 *   OperationLogContext.setAfterData(userMapper.selectById(id));
 * </pre>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public final class OperationLogContext {

    private static final ThreadLocal<Object> BEFORE_DATA = new ThreadLocal<>();
    private static final ThreadLocal<Object> AFTER_DATA = new ThreadLocal<>();

    private OperationLogContext() {
    }

    /**
     * 设置修改前的数据对象
     */
    public static void setBeforeData(Object data) {
        BEFORE_DATA.set(data);
    }

    /**
     * 设置修改后的数据对象
     */
    public static void setAfterData(Object data) {
        AFTER_DATA.set(data);
    }

    /**
     * 获取修改前的数据对象
     */
    public static Object getBeforeData() {
        return BEFORE_DATA.get();
    }

    /**
     * 获取修改后的数据对象
     */
    public static Object getAfterData() {
        return AFTER_DATA.get();
    }

    /**
     * 清除上下文（AOP 切面 finally 中调用）
     */
    public static void clear() {
        BEFORE_DATA.remove();
        AFTER_DATA.remove();
    }
}
