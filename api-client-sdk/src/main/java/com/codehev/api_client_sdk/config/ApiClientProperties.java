package com.codehev.api_client_sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="https://github.com/codehev">codehev</a>
 * @email codehev@qq.com
 * @date 2025/01/13 21:37
 * @description
 */
@Data
// 指定配置前缀
@ConfigurationProperties("api.client")
public class ApiClientProperties {
    // 设置默认值
    private String accessKey = "codehev";
    private String secretKey = "123abc";
    private String host = "127.0.0.1";
    private Integer port = 8102;
}
