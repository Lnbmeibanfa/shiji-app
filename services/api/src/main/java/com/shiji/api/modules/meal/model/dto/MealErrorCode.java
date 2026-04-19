package com.shiji.api.modules.meal.model.dto;

import com.shiji.api.common.web.ErrorCode;
import lombok.Getter;

@Getter
public enum MealErrorCode implements ErrorCode {
    MEAL_FILE_NOT_FOUND(30001, "图片文件不存在或不可用"),
    MEAL_FILE_FORBIDDEN(30002, "无权使用该图片"),
    EMOTION_TAG_INVALID(30003, "情绪标签无效或已停用"),
    MEAL_REQUEST_INVALID(30004, "请求参数不合法"),
    MEAL_DISH_NOT_FOUND(30005, "标准菜品不存在或已停用"),
    MEAL_DISH_EXPAND_EMPTY(30006, "菜品拆解无可用组成或基础食物缺失");

    private final int code;
    private final String message;

    MealErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
