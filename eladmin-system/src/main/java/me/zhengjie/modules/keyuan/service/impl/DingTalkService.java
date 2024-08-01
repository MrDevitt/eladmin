//package me.zhengjie.modules.keyuan.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
//import com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceHeaders;
//import com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceResponse;
//import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
//import com.dingtalk.open.app.api.security.AuthClientCredential;
//import com.dingtalk.open.app.stream.protocol.event.EventAckStatus;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import me.zhengjie.utils.CacheUtils;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class DingTalkService {
//    private static final String APP_KEY = "dingbrhrlawhvgpyz5t7";
//    private static final String APP_SECRET = "o3CGqTpIzgAHWSv-Qf3OVoxhDoIxE0WnlpMD_P_0S-pWDT4CdgqHei-LPHOSB8Np";
//    private final com.aliyun.dingtalkworkflow_1_0.Client dingTalkWorkflowClient;
//
//    private final com.aliyun.dingtalkoauth2_1_0.Client dingTalkOauthClient;
//
//
//    //    @PostConstruct
//    public void init() {
//        try {
//            log.info("starting OpenDingTalkStreamClientBuilder");
//            OpenDingTalkStreamClientBuilder
//                    .custom()
//                    .credential(new AuthClientCredential(APP_KEY, APP_SECRET))
//                    //注册事件监听
//                    .registerAllEventListener(event -> {
//                        try {
//                            log.info(JSON.toJSONString(event));
//                            String processInstanceId = event.getData().getString("processInstanceId");
//                            log.info(JSON.toJSONString(getEventByProcessInstanceId(processInstanceId)));
//                            return EventAckStatus.SUCCESS;
//                        } catch (Exception e) {
//                            //消费失败
//                            log.error("consume event error", e);
//                            return EventAckStatus.LATER;
//                        }
//                    })
//                    .build().
//                    start();
//        } catch (Exception e) {
//            log.error("OpenDingTalkStreamClientBuilder error", e);
//        }
//    }
//
//    public String getAccessToken() {
//        String token = CacheUtils.EXPIRE_CACHE.getIfPresent("accessToken");
//        if (token != null) {
//            return token;
//        }
//        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest()
//                .setAppKey(APP_KEY)
//                .setAppSecret(APP_SECRET);
//        try {
//            token = dingTalkOauthClient.getAccessToken(getAccessTokenRequest).getBody().getAccessToken();
//            CacheUtils.EXPIRE_CACHE.put("accessToken", token);
//        } catch (Exception _err) {
//            log.error("getAccessToken error", _err);
//        }
//        return token;
//    }
//
//    public GetProcessInstanceResponse getEventByProcessInstanceId(String processInstanceId) {
//        GetProcessInstanceHeaders getProcessInstanceHeaders = new GetProcessInstanceHeaders();
//        getProcessInstanceHeaders.xAcsDingtalkAccessToken = getAccessToken();
//        com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceRequest getProcessInstanceRequest = new com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceRequest()
//                .setProcessInstanceId(processInstanceId);
//        try {
//            return dingTalkWorkflowClient.getProcessInstanceWithOptions(getProcessInstanceRequest, getProcessInstanceHeaders, new com.aliyun.teautil.models.RuntimeOptions());
//        } catch (Exception _err) {
//            log.error("getEventById error", _err);
//        }
//        return null;
//    }
//}
