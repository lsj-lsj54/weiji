package com.weiji.framework.security; // 基础设施包（安全/缓存/Redis）

public final class UserContext { // 定义类 UserContext

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>(); // 成员字段

    private UserContext() { // 禁止实例化
    }

    public static void set(LoginUser user) { // 方法 set
        HOLDER.set(user); // 本行业务语句
    }

    public static LoginUser get() { // 方法 get
        return HOLDER.get(); // 返回结果
    }

    public static Long userId() { // 方法 userId
        LoginUser user = HOLDER.get(); // 赋值或调用
        return user == null ? null : user.userId(); // 返回结果
    }

    public static void clear() { // 方法 clear
        HOLDER.remove(); // 本行业务语句
    }
}
