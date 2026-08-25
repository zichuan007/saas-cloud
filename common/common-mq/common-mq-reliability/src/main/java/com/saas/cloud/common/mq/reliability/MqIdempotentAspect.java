package com.saas.cloud.common.mq.reliability;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;

import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.mq.annotation.MqIdempotent;
import com.saas.cloud.common.mq.reliability.entity.MqConsumeLog;
import com.saas.cloud.common.mq.reliability.enums.ConsumeStatus;
import com.saas.cloud.common.mq.reliability.mapper.MqConsumeLogMapper;
import com.saas.cloud.common.security.context.TenantContext;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费幂等切面
 * <p>切 {@link MqIdempotent} 标注的 {@code onMessage}，按 (msgId, group) 查/插
 * {@code mq_consume_log}：已成功则跳过；失败/初始化态允许重试。DB 异常 fail-open 不阻断消费。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class MqIdempotentAspect {

    private final MqConsumeLogMapper consumeLogMapper;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 幂等拦截
     *
     * @param pjp 连接点
     * @return 方法返回值；重复成功消息返回 null
     * @throws Throwable 方法异常
     */
    @Around("@annotation(com.saas.cloud.common.mq.annotation.MqIdempotent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        MqIdempotent anno = method.getAnnotation(MqIdempotent.class);
        Object[] args = pjp.getArgs();
        String msgId = evalKey(anno.key(), args);
        // 无幂等键则不启用幂等，直接执行
        if (msgId == null || msgId.isEmpty()) {
            return pjp.proceed();
        }
        String group = resolveGroup(pjp);
        String topic = resolveTopic(args);

        // 幂等日志表无 tenant_id 列，DB 操作须绕过租户过滤
        Long logId;
        try {
            logId = prepareLogIgnoreTenant(msgId, group, topic);
        } catch (DataAccessException de) {
            // 幂等基础设施异常，fail-open：放行消费避免阻断
            log.warn("[MQ-Idempotent] 幂等检查失败，fail-open msgId={}: {}", msgId, de.getMessage());
            return pjp.proceed();
        }
        // 已成功消费的重复消息：跳过
        if (logId == null) {
            log.info("[MQ-Idempotent] 命中幂等，跳过 msgId={}, group={}", msgId, group);
            return null;
        }
        try {
            Object result = pjp.proceed();
            updateStatusIgnoreTenant(logId, ConsumeStatus.CONSUME_SUCCESS, null);
            return result;
        } catch (Throwable e) {
            try {
                updateStatusIgnoreTenant(logId, ConsumeStatus.CONSUME_FAIL, e.getMessage());
            } catch (DataAccessException de) {
                log.warn("[MQ-Idempotent] 状态更新失败 msgId={}: {}", msgId, de.getMessage());
            }
            throw e;
        }
    }

    /**
     * 在忽略租户过滤的上下文中执行幂等预置（mq_consume_log 无 tenant_id 列）
     *
     * @param msgId 消息 ID
     * @param group 消费组
     * @param topic 主题
     * @return 日志主键，null 表示重复成功跳过
     */
    private Long prepareLogIgnoreTenant(String msgId, String group, String topic) {
        return TenantContext.executeWithoutTenant(() -> prepareLog(msgId, group, topic));
    }

    /**
     * 在忽略租户过滤的上下文中更新消费状态
     *
     * @param id     日志主键
     * @param status 状态
     * @param error  错误信息
     */
    private void updateStatusIgnoreTenant(Long id, ConsumeStatus status, String error) {
        TenantContext.executeWithoutTenant(() -> updateStatus(id, status, error));
    }

    /**
     * 预置消费日志：已成功返回 null（跳过）；不存在插入 INIT 返回 id；存在非成功置 INIT 返回 id
     *
     * @param msgId 消息 ID
     * @param group 消费组
     * @param topic 主题
     * @return 日志主键，null 表示重复成功跳过
     */
    private Long prepareLog(String msgId, String group, String topic) {
        MqConsumeLog existing = selectByMsgId(msgId, group);
        if (existing != null
                && ConsumeStatus.CONSUME_SUCCESS.getCode() == existing.getConsumeStatus()) {
            return null;
        }
        if (existing == null) {
            MqConsumeLog row = new MqConsumeLog();
            row.setMsgId(msgId);
            row.setGroupId(group);
            row.setTopic(topic);
            row.setConsumeStatus(ConsumeStatus.INIT.getCode());
            try {
                consumeLogMapper.insert(row);
                return row.getId();
            } catch (DuplicateKeyException dup) {
                // 并发插入：重新查询
                MqConsumeLog retry = selectByMsgId(msgId, group);
                if (retry != null
                        && ConsumeStatus.CONSUME_SUCCESS.getCode() == retry.getConsumeStatus()) {
                    return null;
                }
                return retry == null ? null : resetToInit(retry.getId());
            }
        }
        return resetToInit(existing.getId());
    }

    /**
     * 重置为 INIT 态以允许重试
     *
     * @param id 日志主键
     * @return id
     */
    private Long resetToInit(Long id) {
        MqConsumeLog update = new MqConsumeLog();
        update.setId(id);
        update.setConsumeStatus(ConsumeStatus.INIT.getCode());
        consumeLogMapper.updateById(update);
        return id;
    }

    /**
     * 查询消费日志
     *
     * @param msgId 消息 ID
     * @param group 消费组
     * @return 日志，无则 null
     */
    private MqConsumeLog selectByMsgId(String msgId, String group) {
        LambdaQueryWrapper<MqConsumeLog> wrapper = new LambdaQueryWrapper<MqConsumeLog>()
                .eq(MqConsumeLog::getMsgId, msgId)
                .eq(MqConsumeLog::getGroupId, group)
                .last("LIMIT 1");
        return consumeLogMapper.selectOne(wrapper);
    }

    /**
     * 更新消费状态
     *
     * @param id     日志主键
     * @param status 状态
     * @param error  错误信息
     */
    private void updateStatus(Long id, ConsumeStatus status, String error) {
        LambdaUpdateWrapper<MqConsumeLog> wrapper = new LambdaUpdateWrapper<MqConsumeLog>()
                .eq(MqConsumeLog::getId, id)
                .set(MqConsumeLog::getConsumeStatus, status.getCode())
                .set(MqConsumeLog::getErrorMsg, error);
        consumeLogMapper.update(null, wrapper);
    }

    /**
     * SpEL 求值幂等键，根变量 {@code msg} 指向 onMessage 第一参（信封）
     *
     * @param keyExpr SpEL 表达式
     * @param args    方法参数
     * @return 幂等键
     */
    private String evalKey(String keyExpr, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        EvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("msg", args[0]);
        try {
            Expression exp = parser.parseExpression(keyExpr);
            Object value = exp.getValue(ctx);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            log.warn("[MQ-Idempotent] 幂等键 SpEL 解析失败 {}: {}", keyExpr, e.getMessage());
            return null;
        }
    }

    /**
     * 从目标类 @MqConsumer 解析消费组
     *
     * @param pjp 连接点
     * @return 消费组
     */
    private String resolveGroup(ProceedingJoinPoint pjp) {
        Class<?> clazz = ClassUtils.getUserClass(pjp.getTarget().getClass());
        MqConsumer meta = clazz.getAnnotation(MqConsumer.class);
        return meta != null ? meta.group() : "default";
    }

    /**
     * 从信封参解析主题
     *
     * @param args 方法参数
     * @return 主题
     */
    private String resolveTopic(Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof MessageEnvelope) {
            return ((MessageEnvelope<?>) args[0]).getTopic();
        }
        return null;
    }
}
