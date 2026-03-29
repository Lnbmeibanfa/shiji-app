package com.shiji.api.modules.auth.model.enums;

/**
 * 短信验证码状态。
 */
public enum SmsCodeStatus {
    PENDING,
    VERIFIED,
    EXPIRED,
    INVALIDATED
}
