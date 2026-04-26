package com.shiji.api.modules.file.model.dto;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public enum FileErrorCode implements ErrorCode {
    FILE_EMPTY(20001, "请选择要上传的文件"),
    FILE_TOO_LARGE(20002, "文件过大"),
    FILE_TYPE_NOT_ALLOWED(20003, "仅支持 JPEG、PNG、WebP 图片"),
    OSS_NOT_CONFIGURED(20004, "对象存储未配置，请联系管理员"),
    OSS_UPLOAD_FAILED(20005, "文件上传失败，请稍后重试"),
    FILE_NOT_FOUND(20006, "文件不存在或已失效"),
    FILE_ACCESS_DENIED(20007, "无权访问该文件");

    private final int code;
    private final String message;

    FileErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
