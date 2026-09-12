package com.weiji.common.utils;

import java.util.UUID;

public final class IdUtils {

    private IdUtils() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
