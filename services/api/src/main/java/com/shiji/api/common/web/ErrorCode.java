package com.shiji.api.common.web;

/**
 * 业务错误码约定：各模块枚举可实现该接口，由 {@link ApiResponse#error(ErrorCode)} 统一封装。
 */
public interface ErrorCode {

    int getCode();

    String getMessage();
}
