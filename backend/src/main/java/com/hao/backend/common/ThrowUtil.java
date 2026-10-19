package com.hao.backend.common;

public class ThrowUtil {
    public static void throwIf(boolean condition,BusinessException exception){
        if(condition){
            throw exception;
        }
    }
    public static void throwIf(boolean condition, ResponseCode responseCode){
        if(condition){
            throw new BusinessException(responseCode);
        }
    }
    public static void throwIf(boolean condition,ResponseCode responseCode,String message){
        if(condition){
            throw new BusinessException(responseCode,message);
        }
    }
}
