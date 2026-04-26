package com.shiji.api.modules.ai.model.dto.response;

import lombok.Builder;

/**
 * 轮询任务状态；成功时 {@link #result} 与同步接口 {@link DishIngredientVisionResponse} 形状一致。
 */
@Builder
public record MealPhotoRecognitionTaskPollResponse(
        String taskId,
        String status,
        DishIngredientVisionResponse result,
        Integer errorCode,
        String errorMessage) {}
