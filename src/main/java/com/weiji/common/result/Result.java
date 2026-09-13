package com.weiji.common.result; // 公共包（结果/异常/常量）

import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import lombok.Data; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
public class Result<T> { // 定义类 Result

    private int code; // 字段 编码
    private String message; // 字段 提示文案
    private T data; // 字段 载荷

    public static <T> Result<T> ok(T data) { // 方法 ok
        Result<T> result = new Result<>(); // 赋值或调用
        result.setCode(ErrorCode.SUCCESS.getCode()); // 成功码 0
        result.setMessage(ErrorCode.SUCCESS.getMessage()); // 成功码 0
        result.setData(data); // 本行业务语句
        return result; // 返回结果
    }

    public static <T> Result<T> ok() { // 方法 ok
        return ok(null); // 返回结果
    }

    public static <T> Result<T> fail(ErrorCode errorCode) { // 方法 fail
        Result<T> result = new Result<>(); // 赋值或调用
        result.setCode(errorCode.getCode()); // 本行业务语句
        result.setMessage(errorCode.getMessage()); // 本行业务语句
        return result; // 返回结果
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) { // 方法 fail
        Result<T> result = new Result<>(); // 赋值或调用
        result.setCode(errorCode.getCode()); // 本行业务语句
        result.setMessage(message); // 本行业务语句
        return result; // 返回结果
    }

    public static <T> Result<T> fail(int code, String message) { // 方法 fail
        Result<T> result = new Result<>(); // 赋值或调用
        result.setCode(code); // 本行业务语句
        result.setMessage(message); // 本行业务语句
        return result; // 返回结果
    }
}
