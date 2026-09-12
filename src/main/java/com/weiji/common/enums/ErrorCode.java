package com.weiji.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_PHONE_EXISTS(1001, "手机号已注册"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USER_PASSWORD_WRONG(1003, "手机号或密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    TOKEN_INVALID(1005, "令牌无效"),
    TOKEN_BLACKLISTED(1006, "令牌已失效"),
    REFRESH_TOKEN_INVALID(1007, "刷新令牌无效"),

    POINT_NOT_ENOUGH(2001, "积分不足"),
    POINT_DUPLICATE(2002, "积分已结算"),
    REWARD_LOCKED(2003, "奖励未满足兑换条件或仍在冷却"),
    ITEM_NOT_ENOUGH(2004, "道具不足"),

    TASK_NOT_FOUND(3001, "任务不存在"),
    FOCUS_INVALID(3002, "计时状态不合法"),
    FOCUS_CHEAT(3003, "计时数据校验失败"),
    REST_DAY(3004, "今天是休息日，不推送任务"),
    DND_PERIOD(3005, "当前处于免打扰时段"),

    KNOWLEDGE_NOT_FOUND(4001, "知识点不存在"),

    FRIEND_EXISTS(5001, "已经是好友"),
    TEAM_NOT_FOUND(5002, "队伍不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
