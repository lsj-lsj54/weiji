package com.weiji.common.constant; // 公共包（结果/异常/常量）

public final class RedisKey { // 定义类 RedisKey

    public static final String TOKEN_BLACKLIST = "weiji:token:blacklist:"; // 字符串常量
    public static final String REFRESH_TOKEN = "weiji:token:refresh:"; // 字符串常量
    public static final String USER_SESSION = "weiji:user:session:"; // 字符串常量
    public static final String TASK_TODAY = "weiji:task:today:"; // 字符串常量
    public static final String RANK_WEEK = "weiji:rank:week:"; // 字符串常量
    public static final String KNOWLEDGE_REVIEW_QUEUE = "weiji:knowledge:review:"; // 字符串常量
    public static final String CACHE_PREFIX = "weiji:c:"; // 字符串常量
    public static final String CACHE_INVALIDATE_CHANNEL = "weiji:cache:invalidate"; // weiji:cache:invalidate

    private RedisKey() { // 禁止实例化
    }

    public static String tokenBlacklist(String tokenId) { // 查 Redis 登出黑名单
        return TOKEN_BLACKLIST + tokenId; // 返回结果
    }

    public static String refreshToken(Long userId) { // 方法 refreshToken
        return REFRESH_TOKEN + userId; // 返回结果
    }

    public static String focusLive(Long userId) { // 方法 focusLive
        return "weiji:focus:live:" + userId; // 返回结果
    }

    public static String taskToday(Long userId) { // 方法 taskToday
        return TASK_TODAY + userId; // 返回结果
    }

    public static String weekRank(String weekKey) { // 方法 weekRank
        return RANK_WEEK + weekKey; // 返回结果
    }

    public static String cache(String name, String key) { // 方法 cache
        return CACHE_PREFIX + name + ":" + key; // 返回结果
    }
}
