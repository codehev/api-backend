package com.codehev.api_client_sdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.codehev.api_common.common.BaseResponse;
import com.codehev.api_common.common.ErrorCode;
import com.codehev.api_common.common.ResultUtils;
import com.codehev.api_common.exception.BusinessException;
import com.codehev.api_common.utils.SignUtils;
import com.codehev.api_model.model.entity.User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private String host;
    private Integer port;
    private String baseUrl;

    public ApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.host = "127.0.0.1";
        this.port = 8102;
        this.baseUrl = "http://" + this.host + ":" + this.port;
    }

    public ApiClient(String accessKey, String secretKey, String host, Integer port) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.host = host;
        this.port = port;
        this.baseUrl = "http://" + host + ":" + port;
    }

    private Map<String, String> getHeaders(String body) throws UnsupportedEncodingException {
        HashMap<String, String> headersHashMap = new HashMap<>();
        headersHashMap.put("accessKey", accessKey);
        // 不直接发送密钥
        // headersHashMap.put("secretKey", secretKey);
        headersHashMap.put("nonce", RandomUtil.randomNumbers(5));
        headersHashMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 对参数进行URL编码
        String encodeBody = URLEncoder.encode(body, StandardCharsets.UTF_8.toString());
        headersHashMap.put("sign", SignUtils.genSign(encodeBody, secretKey));
        // 服务端需要获取body，与secretKey去生成签名sign，encodeBody要进行URL编码，避免特殊字符导致中文乱码从而导致验签时生成的sign不一致
        headersHashMap.put("body", encodeBody);
        return headersHashMap;
    }

    public BaseResponse<String> getNameByGet(String userName) {
        Map<String, String> headers;
        try {
            headers = getHeaders(userName);
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数编码错误");
        }
        //form() 方法不仅适用于 POST 请求的表单参数，也适用于 GET 请求的 URL 参数。Hutool 会自动将 form() 方法添加的参数拼接到 GET 请求的 URL 中。
        HttpResponse httpResponse = HttpRequest.get(baseUrl+"/api/name/")
                .addHeaders(headers)
                .form("userName", userName).execute();
        if (httpResponse.getStatus() != 200) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
        }
        String result = httpResponse.body();
        return ResultUtils.success(result);
    }

    public BaseResponse<String> getUserNameByPost(User user) {
        String json = JSONUtil.toJsonStr(user);
        Map<String, String> headers;
        try {
            headers = getHeaders(json);
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "参数编码错误");
        }

        HttpResponse httpResponse = HttpRequest.post(baseUrl+"/api/name/")
                .header("Content-Type", "application/json; charset=UTF-8")
                .addHeaders(headers)
                .body(json)
                .execute();

        if (httpResponse.getStatus() != 200) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
        }
        String result = httpResponse.body();
        return ResultUtils.success(result);

    }
}
