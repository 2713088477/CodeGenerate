package com.hao.backend.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private Integer code;

    public BusinessException(Integer code){
        this.code = code;
    }

    public BusinessException(Integer code,String message){
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode responseCode){
        this(responseCode.getCode(), responseCode.getMessage());
    }

    public BusinessException(ResponseCode responseCode, String message){
        this(responseCode.getCode(),message);
    }

}
