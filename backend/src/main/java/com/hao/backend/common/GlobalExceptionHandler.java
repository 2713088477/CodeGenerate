package com.hao.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        return new BaseResponse<>(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(item -> item != null && !item.isBlank())
                .findFirst()
                .orElse(ResponseCode.PARAMS_ERROR.getMessage());
        return new BaseResponse<>(ResponseCode.PARAMS_ERROR.getCode(), message, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public BaseResponse<?> handleAuthenticationException(AuthenticationException e) {
        return new BaseResponse<>(ResponseCode.UNAUTHORIZED.getCode(), "账号或密码错误", null);
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("未知的异常,错误信息:{}", e.getMessage());
        return new BaseResponse<>(
                ResponseCode.INTERNAL_SERVER_ERROR.getCode(),
                ResponseCode.INTERNAL_SERVER_ERROR.getMessage(),
                null
        );
    }

}
