package com.shiji.api.modules.auth.service.exception;

import com.shiji.api.modules.auth.model.dto.AuthErrorCode;
import lombok.Getter;

@Getter
public class AuthBusinessException extends RuntimeException {

    private final AuthErrorCode errorCode;

    public AuthBusinessException(AuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
