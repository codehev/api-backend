package com.codehev.api_interface.controller;


import com.codehev.api_interface.model.User;
import com.codehev.api_interface.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 名称 API
 *
 * @author yupi
 */
@RestController
@RequestMapping("name")
public class UserController {
    /**
     * pathName是/api/name/（不能少了最后的/）
     * http://127.0.0.1:8101/api/name/?userName=123
     *
     * @param userName
     * @return
     */
    @GetMapping("/")
    public String getNameByGet(@RequestParam String userName) {
        return userName;
    }


    @PostMapping("/")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        // 从请求头中获取参数
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        // todo 实际情况应该是去数据库中查是否已分配给用户
        if (!accessKey.equals("codehev")) {
            throw new RuntimeException("无权限");
        }
        // todo 校验随机数，模拟一下，直接判断nonce是否大于10000
        if (Long.parseLong(nonce) > 100000) {
            throw new RuntimeException("无权限");
        }

        // todo 时间和当前时间不能超过5分钟
        if (System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) / 1000 > 60 * 5) {
            throw new RuntimeException("无权限");
        }
        // todo 生成签名，判断签名是否正确
        // secretKey从数据库中查询
        String genSign = SignUtils.genSign(body, "123abc");
        if (!sign.equals(genSign)) {
            throw new RuntimeException("无权限");
        }

        return user.getUserName();
    }
}