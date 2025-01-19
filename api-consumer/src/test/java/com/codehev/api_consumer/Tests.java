package com.codehev.api_consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.junit.jupiter.api.Test;


class Tests {

    @Test
    void contextLoads() {
        String url = "https://example.com/api/submit";
        HttpRequest request = HttpRequest.post(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("param1=value1&param2=value2");
        // 打印请求头
        System.out.println("Content-Type: " + request.header("Content-Type"));
        HttpResponse response = request.execute();
        System.out.println(response.body());
    }

}
