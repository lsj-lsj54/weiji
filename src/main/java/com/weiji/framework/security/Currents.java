package com.weiji.framework.security;

import com.weiji.common.enums.ErrorCode;
import com.weiji.common.exception.BizException;

public final class Currents {

    private Currents() {
    }

    public static Long userId() {
        Long userId = UserContext.userId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
