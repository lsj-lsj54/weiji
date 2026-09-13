package com.weiji.common.utils; // 公共包（结果/异常/常量）

import java.util.UUID; // JDK 集合/工具

public final class IdUtils { // 定义类 IdUtils

    private IdUtils() { // 禁止实例化
    }

    public static String uuid() { // 方法 uuid
        return UUID.randomUUID().toString().replace("-", ""); // 返回结果
    }
}
