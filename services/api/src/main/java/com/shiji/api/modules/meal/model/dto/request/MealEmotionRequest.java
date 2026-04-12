package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealEmotionRequest {

    @NotNull
    private Long emotionTagId;

    private Integer emotionIntensity;

    private String remark;
}
