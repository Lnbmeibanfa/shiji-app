package com.shiji.api.modules.ai.service.exception;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public class AiBusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public AiBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AiBusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
