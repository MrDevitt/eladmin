package me.zhengjie.modules.keyuan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 1. 打印请求信息
        log.info("URI         : {}", request.getURI());
        log.info("Method      : {}", request.getMethod());
        log.info("Headers     : {}", request.getHeaders());
        log.info("Request Body: {}", new String(body, StandardCharsets.UTF_8));

        // 2. 执行请求
        ClientHttpResponse response = execution.execute(request, body);

        // 3. 打印响应信息 (可选，需要注意Body流的处理)
        // 注意：这里读取body后，response的流就关闭了，需要使用BufferingClientHttpRequestFactory包装
        log.info("Response Status: {}", response.getStatusCode());

        return response;
    }
}

