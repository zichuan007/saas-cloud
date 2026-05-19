package com.saas.cloud.wechat.oa.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号 API 客户端
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatApiClient {

    private static final String BASE_URL = "https://api.weixin.qq.com/cgi-bin";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;

    /**
     * 获取 AccessToken
     */
    public Map<String, Object> getAccessToken(String appId, String appSecret) {
        String url = BASE_URL + "/token?grant_type=client_credential&appid=" + appId
                + "&secret=" + appSecret;
        return doGet(url);
    }

    /**
     * 获取粉丝列表（每次最多 10000）
     */
    public Map<String, Object> getUserList(String accessToken, String nextOpenid) {
        String url = BASE_URL + "/user/get?access_token=" + accessToken;
        if (nextOpenid != null && !nextOpenid.isEmpty()) {
            url += "&next_openid=" + nextOpenid;
        }
        return doGet(url);
    }

    /**
     * 获取粉丝详情
     */
    public Map<String, Object> getUserInfo(String accessToken, String openid) {
        String url = BASE_URL + "/user/info?access_token=" + accessToken
                + "&openid=" + openid + "&lang=zh_CN";
        return doGet(url);
    }

    /**
     * 批量获取粉丝详情（最多 100 个）
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> batchGetUserInfo(String accessToken, List<String> openids) {
        String url = BASE_URL + "/user/info/batchget?access_token=" + accessToken;
        List<Map<String, String>> userList = new ArrayList<>();
        for (String openid : openids) {
            Map<String, String> item = new HashMap<>();
            item.put("openid", openid);
            item.put("lang", "zh_CN");
            userList.add(item);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("user_list", userList);
        Map<String, Object> result = doPost(url, body);
        Object infoList = result.get("user_info_list");
        if (infoList instanceof List) {
            return (List<Map<String, Object>>) infoList;
        }
        return new ArrayList<>();
    }

    /**
     * 创建标签
     */
    public Map<String, Object> createTag(String accessToken, String tagName) {
        String url = BASE_URL + "/tags/create?access_token=" + accessToken;
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", tagName);
        Map<String, Object> body = new HashMap<>();
        body.put("tag", tag);
        return doPost(url, body);
    }

    /**
     * 获取标签列表
     */
    public Map<String, Object> getTags(String accessToken) {
        String url = BASE_URL + "/tags/get?access_token=" + accessToken;
        return doGet(url);
    }

    /**
     * 创建菜单
     */
    public Map<String, Object> createMenu(String accessToken, Object menuData) {
        String url = BASE_URL + "/menu/create?access_token=" + accessToken;
        return doPost(url, menuData);
    }

    /**
     * 预览图文消息
     */
    public Map<String, Object> previewMessage(String accessToken, String openid,
                                                String mediaId) {
        String url = BASE_URL + "/message/mass/preview?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("touser", openid);
        body.put("msgtype", "mpnews");
        Map<String, String> mpnews = new HashMap<>();
        mpnews.put("media_id", mediaId);
        body.put("mpnews", mpnews);
        return doPost(url, body);
    }

    /**
     * 上传永久素材
     */
    public Map<String, Object> addMaterial(String accessToken, String type,
                                            byte[] fileBytes, String fileName) {
        String url = BASE_URL + "/material/add_material?access_token=" + accessToken
                + "&type=" + type;
        log.info("上传素材到微信, type={}, fileName={}", type, fileName);
        return doPost(url, new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doGet(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Map<String, Object> result = MAPPER.readValue(response.getBody(), Map.class);
            checkError(result);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信API调用失败, url={}", url, e);
            throw new BusinessException("微信API调用失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doPost(String url, Object body) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            Map<String, Object> result = MAPPER.readValue(response.getBody(), Map.class);
            checkError(result);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信API调用失败, url={}", url, e);
            throw new BusinessException("微信API调用失败: " + e.getMessage());
        }
    }

    private void checkError(Map<String, Object> result) {
        Object errcode = result.get("errcode");
        if (errcode != null && !errcode.equals(0)) {
            String errmsg = String.valueOf(result.get("errmsg"));
            log.error("微信API返回错误, errcode={}, errmsg={}", errcode, errmsg);
            throw new BusinessException("微信API错误[" + errcode + "]: " + errmsg);
        }
    }
}
