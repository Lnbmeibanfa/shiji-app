package com.shiji.api.modules.auth.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.auth.service.exception.AuthBusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthBusinessException.class)
    public ApiResponse<Void> handleAuthBusinessException(AuthBusinessException exception) {
        return ApiResponse.error(exception.getErrorCode());
    }
}
