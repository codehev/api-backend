package com.codehev.api_server.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codehev.api_client_sdk.client.ApiClient;
import com.codehev.api_common.common.BaseResponse;
import com.codehev.api_common.common.ErrorCode;
import com.codehev.api_common.common.ResultUtils;
import com.codehev.api_server.annotation.AuthCheck;
import com.codehev.api_server.common.DeleteRequest;
import com.codehev.api_server.common.IdRequest;
import com.codehev.api_server.constant.UserConstant;
import com.codehev.api_common.exception.BusinessException;
import com.codehev.api_common.exception.ThrowUtils;
import com.codehev.api_server.model.dto.interfaceInfo.*;
import com.codehev.api_server.model.entity.InterfaceInfo;
import com.codehev.api_server.model.entity.User;
import com.codehev.api_server.model.enums.InterfaceInfoStatusEnum;
import com.codehev.api_server.model.vo.InterfaceInfoVO;
import com.codehev.api_server.service.InterfaceInfoService;
import com.codehev.api_server.service.UserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 接口管理
 *
 * @author codehev
 * @email codehev@qq.com
 * @createDate 20224-11-24
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private ApiClient apiClient;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 发布接口（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<?> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(idRequest.getId());
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }

        // 2. 判断该接口是否可以被调用
        // todo 固定方法改为根据测试地址来调用
//        com.codehev.api_common.model.interface_entity.User user = new com.codehev.api_common.model.interface_entity.User();
//        user.setUserName("测试");
//        user.setAge(18);
//        String name = apiClient.getUserNameByPost(user);
//        ThrowUtils.throwIf(name == null, ErrorCode.OPERATION_ERROR, "接口连通性测试失败");

        // 通过反射动态获取接口方法
        String interfaceInfoName = interfaceInfo.getName();
        String requestParams = interfaceInfo.getRequestParams();
        Class<? extends ApiClient> apiClientClass = apiClient.getClass();
        Method[] classMethods = apiClientClass.getMethods();
        for (Method method : classMethods) {
            if (method.getName().equals(interfaceInfoName)) {
                try {
                    // 目前不是restful风格，只发GET、POST请求。GET 请求的参数会直接拼接到URL中（代码中GET请求的params 是一个对象，会自动转为URL 中的 query string），而POST请求的参数会放在请求体中（json）。但后端都是通过对象来接收
                    // 测试接口api接收InterfaceInfoInvokeRequest对象，其中userRequestParams属性是当前测试接口的参数json字符串（GET或POST的参数）
                    // todo 目前做的比较简单，支持非restful风格，只发GET、POST请求，后期再扩展（参考Knife4j）
                    BaseResponse<?> baseResponse;
                    if (JSONUtil.isTypeJSON(requestParams)) {
                        //是否为JSON类型字符串，首尾都为大括号或中括号判定为JSON字符串
                        baseResponse = (BaseResponse<?>) method.invoke(apiClient, GSON.fromJson(requestParams, method.getParameterTypes()[0]));
                    } else {
                        //解析不了就是字符串，非封装对象，对于GET请求一个参数直接写字符串，多个参数写json对象，后端封装对象接收
                        baseResponse = (BaseResponse<?>) method.invoke(apiClient, requestParams);
                    }
                    if (baseResponse.getCode() != 0) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口连通性测试失败");
                    }
                    break;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口连通性测试异常");
                }
            }
        }


        // 3. 修改数据接口表的status字段为1（表示开启）
        InterfaceInfo info = new InterfaceInfo();
        info.setId(idRequest.getId());
        info.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean updated = interfaceInfoService.updateById(info);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新数据失败");

        return ResultUtils.success(updated);
    }

    /**
     * 下线接口（仅管理员）
     *
     * @param idRequest id
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验接口是否存在、
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(idRequest.getId());
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }

        // 2. 修改数据接口表的status字段为0（表示下线）
        InterfaceInfo info = new InterfaceInfo();
        info.setId(idRequest.getId());
        info.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean updated = interfaceInfoService.updateById(info);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新数据失败");

        return ResultUtils.success(updated);
    }

    /**
     * 测试接口
     *
     * @param interfaceInfoInvokeRequest id
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<?> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoInvokeRequest.getId());
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }
        // 2. 判断接口是否开启
        if (!interfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.ONLINE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }
        // 3. 调用接口 TODO 固定方法名改为根据测试地址来调用
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        ApiClient tempApiClient = new ApiClient(accessKey, secretKey);

        // 通过反射动态获取接口方法
        String interfaceInfoName = interfaceInfo.getName();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        Class<? extends ApiClient> tempApiClientClass = tempApiClient.getClass();
        Method[] classMethods = tempApiClientClass.getMethods();
        for (Method method : classMethods) {
            if (method.getName().equals(interfaceInfoName)) {
                try {
                    // 目前不是restful风格，只发GET、POST请求。GET 请求的参数会直接拼接到URL中（代码中GET请求的params 是一个对象，会自动转为URL 中的 query string），而POST请求的参数会放在请求体中（json）。但后端都是通过对象来接收
                    // 测试接口api接收InterfaceInfoInvokeRequest对象，其中userRequestParams属性是当前测试接口的参数json字符串（GET或POST的参数）
                    // todo 目前做的比较简单，支持非restful风格，只发GET、POST请求，后期再扩展（参考Knife4j）
                    if (JSONUtil.isTypeJSON(userRequestParams)) {
                        return (BaseResponse<?>) method.invoke(tempApiClient, GSON.fromJson(userRequestParams, method.getParameterTypes()[0]));
                    }
                    //解析不了就是字符串，非封装对象，对于GET请求一个参数直接写字符串，多个参数写json对象，后端封装对象接收
                    return (BaseResponse<?>) method.invoke(tempApiClient, userRequestParams);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用异常");
                }
            }
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "接口调用失败");
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                     HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listMyInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                           HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        interfaceInfoQueryRequest.setUserId(loginUser.getId());
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param interfaceInfoEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editInterfaceInfo(@RequestBody InterfaceInfoEditRequest interfaceInfoEditRequest, HttpServletRequest request) {
        if (interfaceInfoEditRequest == null || interfaceInfoEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoEditRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User loginUser = userService.getLoginUser(request);
        long id = interfaceInfoEditRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldInterfaceInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

}
