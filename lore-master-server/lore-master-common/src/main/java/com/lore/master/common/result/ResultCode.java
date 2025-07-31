package com.lore.master.common.result;

import lombok.Getter;

/**
 * 结果状态码枚举
 */
@Getter
public enum ResultCode {

    // 系统通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    NOT_LOGIN(401, "未登录"),
    PERMISSION_DENIED(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    // 用户相关状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_LOCKED(1004, "账号被锁定"),
    PHONE_NUMBER_ERROR(1005, "手机号格式错误"),
    EMAIL_ERROR(1006, "邮箱格式错误"),
    VERIFY_CODE_ERROR(1007, "验证码错误"),
    VERIFY_CODE_EXPIRED(1008, "验证码过期"),

    // 学习目标相关状态码
    LEARNING_GOAL_NOT_FOUND(2001, "学习目标不存在"),
    LEARNING_GOAL_ALREADY_EXISTS(2002, "学习目标已存在"),
    LEARNING_GOAL_STATUS_ERROR(2003, "学习目标状态错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ResultCode getByCode(int code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.code == code) {
                return resultCode;
            }
        }
        return null;
    }
}