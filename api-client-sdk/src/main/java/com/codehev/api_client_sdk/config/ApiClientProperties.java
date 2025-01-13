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
@ConfigurationProperties("api.client")
public class ApiClientProperties {
    private String accessKey;
    private String secretKey;
    private String host;
    private Integer port;
}
