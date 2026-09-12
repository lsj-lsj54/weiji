package com.weiji.common.utils;

import org.apache.commons.lang3.StringUtils;

public final class PhoneUtils {

    private PhoneUtils() {
    }

    public static boolean isMobile(String phone) {
        return StringUtils.isNotBlank(phone) && phone.matches("^1[3-9]\\d{9}$");
    }
}
