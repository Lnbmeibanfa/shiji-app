package com.shiji.api.modules.ai.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AiExceptionHandler {

    @ExceptionHandler(AiBusinessException.class)
    public ApiResponse<Void> handleAiBusinessException(AiBusinessException exception) {
        return ApiResponse.error(exception.getErrorCode());
    }
}
