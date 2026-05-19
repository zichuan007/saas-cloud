package com.saas.cloud.wechat.oa.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;
import com.saas.cloud.wechat.oa.entity.WechatOaAutoReplyRule;
import com.saas.cloud.wechat.oa.entity.WechatOaFanUser;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.saas.cloud.wechat.oa.service.IWechatOaAutoReplyRuleService;
import com.saas.cloud.wechat.oa.service.IWechatOaFanUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 微信回调控制器（免认证）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaCallbackController {

    private static final byte RULE_TYPE_FOLLOW = 0;
    private static final byte RULE_TYPE_KEYWORD = 1;
    private static final byte RULE_TYPE_DEFAULT = 2;

    private final IWechatOaAccountService accountService;
    private final IWechatOaAutoReplyRuleService autoReplyRuleService;
    private final IWechatOaFanUserService fanUserService;

    /**
     * 微信验证签名
     *
     * @param appId     公众号AppID
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   回声字符串
     * @return 验证通过返回echostr
     */
    @GetMapping("/{appId}")
    public String verify(@PathVariable("appId") String appId,
                         @RequestParam String signature,
                         @RequestParam String timestamp,
                         @RequestParam String nonce,
                         @RequestParam String echostr) {
        WechatOaAccount account = accountService.getByAppId(appId);
        if (account == null) {
            log.warn("回调验证失败，未找到公众号, appId={}", appId);
            return "";
        }

        String token = account.getToken();
        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);
        String content = String.join("", arr);
        String computed = DigestUtil.sha1Hex(content);

        if (computed.equals(signature)) {
            log.info("微信回调验证通过, appId={}", appId);
            return echostr;
        }
        log.warn("微信回调验证失败, appId={}, 签名不匹配", appId);
        return "";
    }

    /**
     * 接收微信推送事件
     *
     * @param appId   公众号AppID
     * @param xmlBody 微信推送的XML消息体
     * @return 回复XML
     */
    @PostMapping("/{appId}")
    public String handleEvent(@PathVariable("appId") String appId,
                              @RequestBody String xmlBody) {
        log.info("收到微信推送事件, appId={}", appId);

        WechatOaAccount account = accountService.getByAppId(appId);
        if (account == null) {
            log.warn("处理推送事件失败，未找到公众号, appId={}", appId);
            return "success";
        }

        String msgType = extractXmlValue(xmlBody, "MsgType");
        String fromUser = extractXmlValue(xmlBody, "FromUserName");
        String toUser = extractXmlValue(xmlBody, "ToUserName");

        if ("event".equals(msgType)) {
            String event = extractXmlValue(xmlBody, "Event");
            return handleWechatEvent(account, event, fromUser, toUser);
        }

        if ("text".equals(msgType)) {
            String content = extractXmlValue(xmlBody, "Content");
            return handleTextMessage(account, content, fromUser, toUser);
        }

        return "success";
    }

    private String handleWechatEvent(WechatOaAccount account, String event,
                                     String fromUser, String toUser) {
        if ("subscribe".equals(event)) {
            handleSubscribe(account, fromUser);
            WechatOaAutoReplyRule rule = autoReplyRuleService.matchRule(
                    account.getId(), RULE_TYPE_FOLLOW, null);
            if (rule != null) {
                return buildTextReply(fromUser, toUser, rule.getReplyContent());
            }
        } else if ("unsubscribe".equals(event)) {
            handleUnsubscribe(account, fromUser);
        }
        return "success";
    }

    private String handleTextMessage(WechatOaAccount account, String content,
                                     String fromUser, String toUser) {
        WechatOaAutoReplyRule rule = autoReplyRuleService.matchRule(
                account.getId(), RULE_TYPE_KEYWORD, content);
        if (rule != null) {
            return buildTextReply(fromUser, toUser, rule.getReplyContent());
        }

        rule = autoReplyRuleService.matchRule(account.getId(), RULE_TYPE_DEFAULT, null);
        if (rule != null) {
            return buildTextReply(fromUser, toUser, rule.getReplyContent());
        }
        return "success";
    }

    private void handleSubscribe(WechatOaAccount account, String openid) {
        WechatOaFanUser fan = fanUserService.getByOpenid(account.getId(), openid);
        if (fan == null) {
            fan = new WechatOaFanUser();
            fan.setAccountId(account.getId());
            fan.setOpenid(openid);
        }
        fan.setSubscribeStatus((byte) 1);
        fan.setSubscribeTime(LocalDateTime.now());
        fan.setUnsubscribeTime(null);
        fanUserService.saveOrUpdateFan(fan);
        log.info("用户关注, appId={}, openid={}", account.getAppId(), openid);
    }

    private void handleUnsubscribe(WechatOaAccount account, String openid) {
        WechatOaFanUser fan = fanUserService.getByOpenid(account.getId(), openid);
        if (fan != null) {
            fan.setSubscribeStatus((byte) 0);
            fan.setUnsubscribeTime(LocalDateTime.now());
            fanUserService.saveOrUpdateFan(fan);
        }
        log.info("用户取关, appId={}, openid={}", account.getAppId(), openid);
    }

    private String extractXmlValue(String xml, String tag) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";
        int start = xml.indexOf(startTag);
        int end = xml.indexOf(endTag);
        if (start < 0 || end < 0) {
            return "";
        }
        String value = xml.substring(start + startTag.length(), end);
        if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
            value = value.substring(9, value.length() - 3);
        }
        return value;
    }

    private String buildTextReply(String toUser, String fromUser, String content) {
        if (StrUtil.isBlank(content)) {
            return "success";
        }
        return "<xml>" +
                "<ToUserName><![CDATA[" + toUser + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + fromUser + "]]></FromUserName>" +
                "<CreateTime>" + (System.currentTimeMillis() / 1000) + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "</xml>";
    }
}
