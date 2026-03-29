package com.shiji.api.common.web;

import lombok.Builder;
import lombok.Getter;

/**
 * 全局 HTTP 统一返回体，符合 <code>openspec/specs/server-architecture</code> 约定。
 *
 * <pre>
 * { "code": 0, "message": "success", "data": {} }
 * </pre>
 */
@Getter
@Builder
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(0).message("success").data(data).build();
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder().code(code).message(message).data(null).build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}
