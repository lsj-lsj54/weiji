package com.weiji.framework.security; // 基础设施包（安全/缓存/Redis）

import com.weiji.common.enums.ErrorCode; // 本仓类 ErrorCode
import com.weiji.common.exception.BizException; // 本仓类 BizException

public final class Currents { // 定义类 Currents

    private Currents() { // 禁止实例化
    }

    public static Long userId() { // 方法 userId
        Long userId = UserContext.userId(); // 赋值或调用
        if (userId == null) { // 条件判断
            throw new BizException(ErrorCode.UNAUTHORIZED); // 抛出业务或运行时异常
        }
        return userId; // 返回结果
    }
}
