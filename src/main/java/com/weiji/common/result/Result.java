package com.weiji.common.result;

import com.weiji.common.enums.ErrorCode;
import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
