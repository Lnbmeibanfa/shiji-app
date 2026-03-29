package com.shiji.api.modules.auth.model.dto;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public enum AuthErrorCode implements ErrorCode {
    SMS_SEND_TOO_FREQUENT(10001, "验证码发送过于频繁"),
    SMS_HOURLY_LIMIT_EXCEEDED(10002, "验证码发送次数超过限制"),
    SMS_CODE_NOT_FOUND(10003, "验证码不存在或已失效"),
    SMS_CODE_EXPIRED(10004, "验证码已过期"),
    SMS_CODE_INVALID(10005, "验证码错误"),
    SMS_CODE_ATTEMPTS_EXCEEDED(10006, "验证码错误次数超限"),
    AGREEMENT_NOT_ACCEPTED(10007, "未同意协议，禁止登录"),
    SESSION_INVALID(10008, "会话无效"),
    UNAUTHORIZED(10009, "未登录或登录已失效");

    private final int code;
    private final String message;

    AuthErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
