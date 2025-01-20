package com.codehev.api_server.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author codehev
 * @email codehev@qq.com
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 请求参数（接送字符串）
     */
    private String userRequestParams;

    private static final long serialVersionUID = 1L;
}