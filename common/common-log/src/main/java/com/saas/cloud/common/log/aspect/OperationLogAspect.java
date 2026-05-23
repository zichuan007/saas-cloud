package com.saas.cloud.common.log.aspect;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.util.IpRegionUtils;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.kafka.producer.KafkaProducerService;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.common.log.event.OperationLogEvent;
import com.saas.cloud.common.security.context.UserContext;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志 AOP 切面
 * 采集请求详细信息，通过 Kafka 异步发送操作日志事件。
 * 当 Kafka 不可用时降级为仅打印日志。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    /** 请求参数 JSON 最大长度，超过则截断 */
    private static final int MAX_PARAMS_LENGTH = 2000;

    /**
     * 条件注入：Kafka 不可用时为 null，降级为日志打印
     */
    @Autowired(required = false)
    private KafkaProducerService kafkaProducerService;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            try {
                sendOperationLog(pjp, opLog, duration, error);
            } catch (Exception ex) {
                log.warn("[操作日志] 发送日志事件失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 构建并发送操作日志事件
     */
    private void sendOperationLog(ProceedingJoinPoint pjp, OperationLog opLog,
                                  long duration, Throwable error) {
        OperationLogEvent event = new OperationLogEvent();
        event.setModule(opLog.module());
        event.setOperation(opLog.operation());
        event.setDuration(duration);
        event.setTimestamp(System.currentTimeMillis());

        // 类名.方法名
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        event.setMethod(className + "." + methodName);

        // 从 HttpServletRequest 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            event.setRequestUrl(request.getRequestURI());
            event.setRequestMethod(request.getMethod());
            String clientIp = getClientIp(request);
            event.setIp(clientIp);
            event.setLocation(IpRegionUtils.getRegion(clientIp));
            event.setUserAgent(request.getHeader("User-Agent"));
        }

        // 从 UserContext 获取用户信息
        UserContext.UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            event.setUserId(userInfo.getUserId());
            event.setUsername(userInfo.getUsername());
            event.setTenantId(userInfo.getTenantId());
        }

        // 序列化请求参数（跳过不可序列化的类型）
        event.setRequestParams(serializeArgs(pjp.getArgs()));

        // 响应状态
        if (error != null) {
            event.setResponseCode(500);
            String errorMsg = error.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            event.setErrorMsg(errorMsg);
        } else {
            event.setResponseCode(200);
        }

        // 通过 Kafka 发送，不可用时通过 Spring 事件同步入库
        if (kafkaProducerService != null && objectMapper != null) {
            try {
                String json = objectMapper.writeValueAsString(event);
                kafkaProducerService.send(KafkaConfig.TOPIC_OPERATION_LOG, json);
                log.debug("[操作日志] 已发送到 Kafka: module={}, operation={}, duration={}ms",
                        opLog.module(), opLog.operation(), duration);
            } catch (Exception e) {
                log.warn("[操作日志] Kafka 发送失败，降级到 Spring 事件: {}", e.getMessage());
                eventPublisher.publishEvent(event);
            }
        } else {
            eventPublisher.publishEvent(event);
            log.debug("[操作日志] Kafka 不可用，通过 Spring 事件同步入库: module={}, operation={}, duration={}ms",
                    opLog.module(), opLog.operation(), duration);
        }
    }

    /**
     * 序列化方法参数为 JSON，跳过 HttpServletRequest/Response/MultipartFile 等类型
     *
     * @param args 方法参数数组
     * @return JSON 字符串，超过 MAX_PARAMS_LENGTH 则截断
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        if (objectMapper == null) {
            return "[参数序列化不可用]";
        }
        try {
            List<Object> filteredArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg == null) {
                    filteredArgs.add(null);
                } else if (arg instanceof ServletRequest
                        || arg instanceof ServletResponse
                        || arg instanceof MultipartFile) {
                    // 跳过不可序列化的类型，记录占位符
                    filteredArgs.add("[" + arg.getClass().getSimpleName() + "]");
                } else {
                    filteredArgs.add(arg);
                }
            }
            String json = objectMapper.writeValueAsString(filteredArgs);
            if (json.length() > MAX_PARAMS_LENGTH) {
                return json.substring(0, MAX_PARAMS_LENGTH) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            log.warn("[操作日志] 参数序列化失败: {}", e.getMessage());
            return "[序列化失败]";
        }
    }

    /**
     * 获取客户端真实 IP，考虑反向代理
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断 IP 字符串是否有效（非空且非 unknown）
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
