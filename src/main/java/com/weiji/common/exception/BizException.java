package com.weiji.common.exception; // 公共包（结果/异常/常量）

import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import lombok.Getter; // Lombok 样板代码生成

@Getter // Lombok 只生成 getter
public class BizException extends RuntimeException { // 定义类 BizException，有继承

    private final ErrorCode errorCode; // 构造注入 errorCode

    public BizException(ErrorCode errorCode) { // 方法 BizException
        super(errorCode.getMessage()); // 本行业务语句
        this.errorCode = errorCode; // 给当前对象赋值
    }

    public BizException(ErrorCode errorCode, String message) { // 方法 BizException
        super(message); // 本行业务语句
        this.errorCode = errorCode; // 给当前对象赋值
    }
}
