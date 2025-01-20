package com.codehev.api_interface.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.codehev.api_common.utils.SignUtils;
import com.codehev.api_interface.model.entity.User;
import com.codehev.api_interface.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 名称 API
 *
 * @author yupi
 */
@RestController
@RequestMapping("name")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * pathName是/api/name/（不能少了最后的/）
     * http://127.0.0.1:8101/api/name/?userName=123
     *
     * @param userName
     * @return
     */
    @GetMapping("/")
    public String getNameByGet(@RequestParam String userName, HttpServletRequest request) {
        apiAuth(request);
        return userName;
    }


    @PostMapping("/")
    public String getUserNameByPost(@RequestBody com.codehev.api_interface.model.entity.User user, HttpServletRequest request) {
        apiAuth(request);
        return user.getUserName();
    }

    /**
     * API认证
     *
     * @param request 请求
     */
    public void apiAuth(HttpServletRequest request) {
        // 从请求头中获取参数
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");

        // 1. todo 实际情况应该是去数据库中查是否已分配给用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(accessKey), "accessKey", accessKey);
        User user = null;
        try {
            user = userService.getOne(queryWrapper);
        } catch (Exception e) {
            throw new RuntimeException("查询失败");
        }
        if (user == null) {
            throw new RuntimeException("无权限");
        }
        // 2. todo 校验随机数，模拟一下，直接判断nonce是否大于10000
        if (Long.parseLong(nonce) > 100000) {
            throw new RuntimeException("无权限");
        }

        // 3. todo 时间和当前时间不能超过5分钟
        if (System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) / 1000 > 60 * 5) {
            throw new RuntimeException("无权限");
        }
        // 4. todo 生成签名，判断签名是否正确。secretKey从数据库中查询
        String genSign = SignUtils.genSign(body, user.getSecretKey());
        if (!sign.equals(genSign)) {
            throw new RuntimeException("无权限");
        }
    }
}