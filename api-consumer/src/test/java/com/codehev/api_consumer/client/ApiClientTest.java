package com.codehev.api_consumer.client;

import com.codehev.api_client_sdk.client.ApiClient;
import com.codehev.api_common.model.interface_entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author <a href="https://github.com/codehev">codehev</a>
 * @email codehev@qq.com
 * @date 2025/01/13 14:06
 * @description
 */
@SpringBootTest
class ApiClientTest {

    @Autowired
    ApiClient apiClient;

    @Test
    void getNameByGet() {
        String nameByGet = apiClient.getNameByGet("codehev");
        System.out.println("nameByGet：" + nameByGet);
    }

    @Test
    void getUserNameByPost() {
        User user = new User();
        user.setUserName("codehev");
        user.setAge(12);
        String userNameByPost = apiClient.getUserNameByPost(user);
        System.out.println("getUserNameByPost：" + userNameByPost);
    }
}