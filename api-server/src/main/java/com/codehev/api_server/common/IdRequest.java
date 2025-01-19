package com.codehev.api_server.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author codehev
 * @email codehev@qq.com
 */
@Data
public class IdRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}