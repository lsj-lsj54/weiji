package com.weiji.common.constant;

public final class RedisKey {

    public static final String TOKEN_BLACKLIST = "weiji:token:blacklist:";
    public static final String REFRESH_TOKEN = "weiji:token:refresh:";
    public static final String USER_SESSION = "weiji:user:session:";
    public static final String TASK_TODAY = "weiji:task:today:";
    public static final String RANK_WEEK = "weiji:rank:week:";
    public static final String KNOWLEDGE_REVIEW_QUEUE = "weiji:knowledge:review:";

    private RedisKey() {
    }

    public static String tokenBlacklist(String tokenId) {
        return TOKEN_BLACKLIST + tokenId;
    }

    public static String refreshToken(Long userId) {
        return REFRESH_TOKEN + userId;
    }
}
