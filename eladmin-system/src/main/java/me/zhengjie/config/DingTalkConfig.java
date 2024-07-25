package me.zhengjie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DingTalkConfig {
    @Bean
    public com.aliyun.dingtalkworkflow_1_0.Client dingTalkWorkflowClient() {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            return new com.aliyun.dingtalkworkflow_1_0.Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public com.aliyun.dingtalkoauth2_1_0.Client dingTalkOauthClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }
}
