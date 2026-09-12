package com.weiji.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }
}
