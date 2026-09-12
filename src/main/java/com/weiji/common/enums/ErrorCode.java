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
    REFRESH_TOKEN_INVALID(1007, "刷新令牌无效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
