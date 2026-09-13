package com.weiji.common.enums; // 公共包（结果/异常/常量）

import lombok.Getter; // Lombok 样板代码生成

@Getter // Lombok 只生成 getter
public enum UserStatus { // 枚举 UserStatus
    DISABLED(0), // 本行业务语句
    ENABLED(1); // 本行业务语句

    private final int code; // 构造注入 编码

    UserStatus(int code) { // 开始代码块
        this.code = code; // 给当前对象赋值
    }
}
