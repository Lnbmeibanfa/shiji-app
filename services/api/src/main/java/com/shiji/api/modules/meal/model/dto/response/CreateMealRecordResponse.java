package com.shiji.api.modules.meal.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMealRecordResponse {

    private final Long mealRecordId;
}
