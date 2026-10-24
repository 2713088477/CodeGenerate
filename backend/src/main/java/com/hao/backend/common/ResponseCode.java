package com.hao.backend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举类
 * 自定义响应码，便于区分与SpringBoot自带的响应码，但前缀语义基本相同，便于理解
 * ResponseCode
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS(0,"成功"),

    UNAUTHORIZED(40001, "未授权"),
    BAD_REQUEST(40002, "请求错误"),
    NOT_FOUND(40003, "未找到"),

    INTERNAL_SERVER_ERROR(50001, "服务器错误"),
    SERVICE_UNAVAILABLE(50002, "服务不可用"),
    GATEWAY_TIMEOUT(50003, "网络超时"),
    REQUEST_ERROR(50005,"请求错误"),
    
    PARAMS_ERROR(40010, "参数错误"),
    ;
    
    private final Integer code;
    private final String message;

}
