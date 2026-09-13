package com.weiji.common.utils; // 公共包（结果/异常/常量）

import org.apache.commons.lang3.StringUtils; // 字符串工具

public final class PhoneUtils { // 定义类 PhoneUtils

    private PhoneUtils() { // 禁止实例化
    }

    public static boolean isMobile(String phone) { // 方法 isMobile
        return StringUtils.isNotBlank(phone) && phone.matches("^1[3-9]\\d{9}$"); // token 非空才解析
    }
}
