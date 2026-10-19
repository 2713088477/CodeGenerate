package com.hao.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseResponse<T> {
    Integer code;
    String message;
    T data;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResponseCode.SUCCESS.getCode(), "success", data);
    }
    public static <T> BaseResponse<T> success() {
        return success(null);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(ResponseCode.REQUEST_ERROR.getCode(), message, null);
    }

}
