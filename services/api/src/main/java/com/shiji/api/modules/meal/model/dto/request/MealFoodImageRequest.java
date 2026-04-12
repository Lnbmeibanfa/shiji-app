package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealFoodImageRequest {

    @NotNull
    private Long fileId;

    @NotNull
    private Integer isPrimary;

    @NotNull
    private Integer sortOrder;
}
