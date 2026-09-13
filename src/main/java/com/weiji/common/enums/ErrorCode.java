package com.weiji.common.enums; // 公共包（结果/异常/常量）

import lombok.Getter; // Lombok 样板代码生成

@Getter // Lombok 只生成 getter
public enum ErrorCode { // 枚举 ErrorCode

    SUCCESS(0, "ok"), // 本行业务语句
    BAD_REQUEST(400, "请求参数错误"), // 本行业务语句
    UNAUTHORIZED(401, "未登录或登录已过期"), // 本行业务语句
    FORBIDDEN(403, "没有权限"), // 本行业务语句
    NOT_FOUND(404, "资源不存在"), // 本行业务语句
    CONFLICT(409, "资源冲突"), // 本行业务语句
    TOO_MANY_REQUESTS(429, "请求过于频繁"), // 本行业务语句
    INTERNAL_ERROR(500, "服务器内部错误"), // 本行业务语句

    USER_PHONE_EXISTS(1001, "手机号已注册"), // 本行业务语句
    USER_NOT_FOUND(1002, "用户不存在"), // 本行业务语句
    USER_PASSWORD_WRONG(1003, "手机号或密码错误"), // 本行业务语句
    USER_DISABLED(1004, "账号已被禁用"), // 本行业务语句
    TOKEN_INVALID(1005, "令牌无效"), // 令牌无效或过期
    TOKEN_BLACKLISTED(1006, "令牌已失效"), // 令牌已登出
    REFRESH_TOKEN_INVALID(1007, "刷新令牌无效"), // 令牌无效或过期

    POINT_NOT_ENOUGH(2001, "积分不足"), // 本行业务语句
    POINT_DUPLICATE(2002, "积分已结算"), // 本行业务语句
    REWARD_LOCKED(2003, "奖励未满足兑换条件或仍在冷却"), // 本行业务语句
    ITEM_NOT_ENOUGH(2004, "道具不足"), // 本行业务语句

    TASK_NOT_FOUND(3001, "任务不存在"), // 本行业务语句
    FOCUS_INVALID(3002, "计时状态不合法"), // 本行业务语句
    FOCUS_CHEAT(3003, "计时数据校验失败"), // 本行业务语句
    REST_DAY(3004, "今天是休息日，不推送任务"), // 本行业务语句
    DND_PERIOD(3005, "当前处于免打扰时段"), // 本行业务语句

    KNOWLEDGE_NOT_FOUND(4001, "知识点不存在"), // 本行业务语句

    FRIEND_EXISTS(5001, "已经是好友"), // 本行业务语句
    TEAM_NOT_FOUND(5002, "队伍不存在"); // 本行业务语句

    private final int code; // 构造注入 编码
    private final String message; // 构造注入 提示文案

    ErrorCode(int code, String message) { // 开始代码块
        this.code = code; // 给当前对象赋值
        this.message = message; // 给当前对象赋值
    }
}
