package me.zhengjie.modules.keyuan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.utils.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DingTalkService {

    private RestTemplate restTemplate = new RestTemplate();

    private final RateLimiter rateLimiter = RateLimiter.create(5);

    private static final String ACCESS_TOKEN_URL = "https://api.dingtalk.com/v1.0/oauth2/accessToken";
    private static final String MESSAGE_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";

    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appSecret;

    @Value("${dingtalk.agent_id}")
    private String agentId;

    @Value("${dingtalk.debug}")
    private boolean debug;
    private String accessToken;


    @PostConstruct
    public void init() {
        refreshAccessToken();
        if (debug) {
            restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
        }
    }

    public void refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("appKey", appKey);
        requestBody.put("appSecret", appSecret);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            accessToken = restTemplate.postForObject(ACCESS_TOKEN_URL, requestEntity, JSONObject.class).getString("accessToken");
            log.info("获取access_token成功,token=" + accessToken);
        } catch (Exception e) {
            log.error("获取access_token失败", e);
        }
    }

    public void sendMessage(JSONObject message, String userIdList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("agent_id", agentId);
        requestBody.put("userid_list", userIdList);
        requestBody.put("msg", message);
        String url = MESSAGE_URL + "?access_token=" + accessToken;
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            rateLimiter.acquire();
            restTemplate.postForObject(url, requestEntity, JSONObject.class, accessToken);
        } catch (Exception e) {
            log.error("发送钉钉消息失败", e);
        }
    }

//    public static void main(String[] args) {
//        DingTalkService service = new DingTalkService();
//        RestTemplate restTemplate = new RestTemplate(
//                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
//        );
//
//        // 注册拦截器
//        restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
//        DingTalkService.restTemplate = restTemplate;
//        service.setAppKey("dingbrhrlawhvgpyz5t7");
//        service.setAppSecret("o3CGqTpIzgAHWSv-Qf3OVoxhDoIxE0WnlpMD_P_0S-pWDT4CdgqHei-LPHOSB8Np");
//        service.setAgentId("3166899467");
//        service.init();
//        JSONObject message = new JSONObject();
//        message.put("msgtype", "text");
//        JSONObject text = new JSONObject();
//        text.put("content", "测试消息22");
//        message.put("text", text);
//        log.info(message.toJSONString());
//        service.sendMessage(message, "3253152364991358,1946596041989985");
//    }
}
