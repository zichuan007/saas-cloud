package com.saas.cloud.notify.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saas.cloud.notify.entity.SmsLog;
import com.saas.cloud.notify.mapper.SmsLogMapper;
import com.saas.cloud.notify.service.ISmsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信发送服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SmsServiceImpl implements ISmsService {

    private final SmsLogMapper smsLogMapper;

    @Override
    public void sendSms(String phone, String templateCode, Map<String, String> params) {

        SmsBlend smsBlend = SmsFactory.getSmsBlend();
        if (smsBlend == null) {
            log.warn("[短信] 未配置短信通道，跳过发送, phone={}", phone);
            return;
        }

        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(phone);
        smsLog.setTemplateCode(templateCode);
        smsLog.setChannel(smsBlend.getSupplier());
        smsLog.setContent(params != null ? params.toString() : "");
        smsLog.setSendTime(LocalDateTime.now());

        try {
            LinkedHashMap<String, String> templateParams =
                    params != null ? new LinkedHashMap<>(params) : new LinkedHashMap<>();
            SmsResponse response = smsBlend.sendMessage(phone, templateCode, templateParams);

            if (response.isSuccess()) {
                smsLog.setStatus((byte) 1);
                smsLog.setBizId(String.valueOf(response.getData()));
                log.info("[短信] 发送成功, phone={}, templateCode={}", phone, templateCode);
            } else {
                smsLog.setStatus((byte) 0);
                smsLog.setErrorMsg(String.valueOf(response.getData()));
                log.warn("[短信] 发送失败, phone={}, templateCode={}, data={}",
                        phone, templateCode, response.getData());
            }
        } catch (Exception e) {
            smsLog.setStatus((byte) 0);
            smsLog.setErrorMsg(e.getMessage());
            log.error("[短信] 发送异常, phone={}, templateCode={}", phone, templateCode, e);
        }

        smsLogMapper.insert(smsLog);
    }

    @Override
    public void sendSmsContent(String phone, String content) {

        SmsBlend smsBlend = SmsFactory.getSmsBlend();
        if (smsBlend == null) {
            log.warn("[短信] 未配置短信通道，跳过发送, phone={}", phone);
            return;
        }

        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(phone);
        smsLog.setContent(content);
        smsLog.setChannel(smsBlend.getSupplier());
        smsLog.setSendTime(LocalDateTime.now());

        try {
            SmsResponse response = smsBlend.sendMessage(phone, content);

            if (response.isSuccess()) {
                smsLog.setStatus((byte) 1);
                smsLog.setBizId(String.valueOf(response.getData()));
                log.info("[短信] 发送成功, phone={}", phone);
            } else {
                smsLog.setStatus((byte) 0);
                smsLog.setErrorMsg(String.valueOf(response.getData()));
                log.warn("[短信] 发送失败, phone={}, data={}", phone, response.getData());
            }
        } catch (Exception e) {
            smsLog.setStatus((byte) 0);
            smsLog.setErrorMsg(e.getMessage());
            log.error("[短信] 发送异常, phone={}", phone, e);
        }

        smsLogMapper.insert(smsLog);
    }

}
