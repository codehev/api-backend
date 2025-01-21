package com.codehev.api_interface.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.codehev.api_common.common.BaseResponse;
import com.codehev.api_common.common.ErrorCode;
import com.codehev.api_common.common.ResultUtils;
import com.codehev.api_common.exception.BusinessException;
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
     * http://127.0.0.1:8102/api/name/?userName=123
     *
     * @param userName
     * @return
     */
    @GetMapping("/")
    public BaseResponse<String> getNameByGet(@RequestParam String userName, HttpServletRequest request) {
        if (StringUtils.isBlank(userName)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        apiAuth(request);
        return ResultUtils.success(userName);
    }

    /**
     * 获取用户名
     *
     * @param user
     * @param request
     * @return
     */

    @PostMapping("/")
    public BaseResponse<String> getUserNameByPost(@RequestBody com.codehev.api_model.model.entity.User user, HttpServletRequest request) {
        if (user == null) {
            ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        apiAuth(request);
        return ResultUtils.success(user.getUserName());
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
        if (StringUtils.isBlank(accessKey)
                || StringUtils.isBlank(nonce)
                || StringUtils.isBlank(timestamp)
                || StringUtils.isBlank(sign)) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求头参数为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求头参数为空");
        }

        // 1. todo 实际情况应该是去数据库中查是否已分配给用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(accessKey), "accessKey", accessKey);
        User user = null;
        try {
            user = userService.getOne(queryWrapper);
        } catch (Exception e) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "accessKey不存在");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "accessKey不存在");
        }
        if (user == null) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无效accessKey");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无效accessKey");
        }
        // 2. todo 校验随机数，模拟一下，直接判断nonce是否大于10000
        if (Long.parseLong(nonce) > 100000 || Long.parseLong(nonce) < 10000) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "无效nonce");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无效nonce");
        }

        // 3. todo 时间和当前时间不能超过5分钟
        if (System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) / 1000 > 60 * 5) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "请求过期");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请求过期");
        }
        // 4. todo 生成签名，判断签名是否正确。secretKey从数据库中查询
        String genSign = SignUtils.genSign(body, user.getSecretKey());
        if (!sign.equals(genSign)) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "验签失败");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "验签失败");
        }
    }
}