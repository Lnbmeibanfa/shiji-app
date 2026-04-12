package com.shiji.api.modules.meal.service.exception;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public class MealBusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public MealBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
