package com.hao.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e){
        return new BaseResponse<>(e.getCode(),e.getMessage(),null);
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e){
        log.error("未知的异常,错误信息:{}",e.getMessage());
        return new BaseResponse<>(
                ResponseCode.INTERNAL_SERVER_ERROR.getCode(),
                ResponseCode.INTERNAL_SERVER_ERROR.getMessage(),
                null
        );
    }

}
