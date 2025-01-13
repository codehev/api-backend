package com.codehev.api_client_sdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.codehev.api_client_sdk.model.User;
import com.codehev.api_client_sdk.utils.SignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/codehev">codehev</a>
 * @email codehev@qq.com
 * @date 2025/01/13 13:48
 * @description
 */

public class ApiClient {
    private String accessKey;
    private String secretKey;

    public ApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    private Map<String, String> getHeaders(String body) {
        HashMap<String, String> headersHashMap = new HashMap<>();
        headersHashMap.put("accessKey", accessKey);
        // 不直接发送密钥
        // headersHashMap.put("secretKey", secretKey);
        headersHashMap.put("nonce", RandomUtil.randomNumbers(5));
        headersHashMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        headersHashMap.put("sign", SignUtils.genSign(body, secretKey));
        // 服务端需要获取body，与secretKey去生成签名
        headersHashMap.put("body", body);
        return headersHashMap;
    }

    public String getNameByGet(String userName) {


        //form() 方法不仅适用于 POST 请求的表单参数，也适用于 GET 请求的 URL 参数。Hutool 会自动将 form() 方法添加的参数拼接到 GET 请求的 URL 中。
        HttpResponse httpResponse = HttpRequest.get("http://127.0.0.1:8101/api/name/")
                .addHeaders(getHeaders(userName))
                .form("userName", userName).execute();
        String body = httpResponse.body();
        return body;
//        方式二
//        // 可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
//        HashMap<String, Object> paramMap = new HashMap<>();
//        // 将"name"参数添加到映射中
//        paramMap.put("name", name);
//        // 使用HttpUtil工具发起GET请求，并获取服务器返回的结果
//        String result = HttpUtil.get("http://localhost:8101/api/name/", paramMap);
//        // 返回服务器返回的结果
//        return result;


    }

    public String getUserNameByPost(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post("http://localhost:8101/api/name/")
                .header("Content-Type", "application/json")
                .addHeaders(getHeaders(json))
                .body(json)
                .execute();
        String result = httpResponse.body();
        return result;

    }
}
