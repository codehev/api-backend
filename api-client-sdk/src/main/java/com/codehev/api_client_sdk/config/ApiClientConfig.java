package com.codehev.api_client_sdk.config;

import com.codehev.api_client_sdk.client.ApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
//确保某些类存在时才启用自动配置
//只有当 ApiClient 类存在于类路径中时，才会加载 MyAutoConfiguration 类。
@ConditionalOnClass(ApiClient.class)
//启用对 @ConfigurationProperties 注解类的支持
//因为ApiClientProperties并没有加类似于@Component的注解标记为bean，没法直接构造注入
@EnableConfigurationProperties(ApiClientProperties.class)
public class ApiClientConfig {

    private final ApiClientProperties apiClientProperties;

    public ApiClientConfig(ApiClientProperties apiClientProperties) {
        this.apiClientProperties = apiClientProperties;
    }

    @Bean
    public ApiClient apiClient() {
        String accessKey = apiClientProperties.getAccessKey();
        String secretKey = apiClientProperties.getSecretKey();
        String host = apiClientProperties.getHost();
        Integer port = apiClientProperties.getPort();

        return new ApiClient(accessKey, secretKey, host, port);
    }
}