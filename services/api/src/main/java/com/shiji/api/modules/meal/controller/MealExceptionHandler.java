package com.shiji.api.modules.meal.controller;

import com.shiji.api.common.web.ApiResponse;
import com.shiji.api.modules.meal.service.exception.MealBusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MealExceptionHandler {

    @ExceptionHandler(MealBusinessException.class)
    public ApiResponse<Void> handleMealBusinessException(MealBusinessException exception) {
        return ApiResponse.error(exception.getErrorCode());
    }
}
