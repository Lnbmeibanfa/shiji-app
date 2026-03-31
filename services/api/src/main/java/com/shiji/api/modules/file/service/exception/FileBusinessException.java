package com.shiji.api.modules.file.service.exception;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public class FileBusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public FileBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
