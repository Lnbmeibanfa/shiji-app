package com.shiji.api.modules.file.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.file.service.exception.FileBusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(FileBusinessException.class)
    public ApiResponse<Void> handleFileBusinessException(FileBusinessException exception) {
        return ApiResponse.error(exception.getErrorCode());
    }
}
