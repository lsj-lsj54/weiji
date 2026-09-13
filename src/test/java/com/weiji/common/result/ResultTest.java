package com.weiji.common.result; // 公共包（结果/异常/常量）

import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import org.junit.jupiter.api.Test; // JUnit 测试

import static org.junit.jupiter.api.Assertions.assertEquals; // JUnit 测试
import static org.junit.jupiter.api.Assertions.assertNull; // JUnit 测试

class ResultTest { // 定义类 ResultTest

    @Test // JUnit 测试方法
    void okWithoutData() { // 开始代码块
        Result<Void> result = Result.ok(); // 成功响应 code=0
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode()); // 成功码 0
        assertNull(result.getData()); // 本行业务语句
    }
}
