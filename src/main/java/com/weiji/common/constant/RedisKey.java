package com.weiji.common.constant;

public final class RedisKey {

    public static final String TOKEN_BLACKLIST = "weiji:token:blacklist:";
    public static final String REFRESH_TOKEN = "weiji:token:refresh:";
    public static final String USER_SESSION = "weiji:user:session:";
    public static final String TASK_TODAY = "weiji:task:today:";
    public static final String RANK_WEEK = "weiji:rank:week:";
    public static final String KNOWLEDGE_REVIEW_QUEUE = "weiji:knowledge:review:";
    public static final String CACHE_PREFIX = "weiji:c:";
    public static final String CACHE_INVALIDATE_CHANNEL = "weiji:cache:invalidate";

    private RedisKey() {
    }

    public static String tokenBlacklist(String tokenId) {
        return TOKEN_BLACKLIST + tokenId;
    }

    public static String refreshToken(Long userId) {
        return REFRESH_TOKEN + userId;
    }

    public static String focusLive(Long userId) {
        return "weiji:focus:live:" + userId;
    }

    public static String taskToday(Long userId) {
        return TASK_TODAY + userId;
    }

    public static String weekRank(String weekKey) {
        return RANK_WEEK + weekKey;
    }

    public static String cache(String name, String key) {
        return CACHE_PREFIX + name + ":" + key;
    }
}
