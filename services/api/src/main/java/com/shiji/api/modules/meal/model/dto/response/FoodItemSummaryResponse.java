package com.shiji.api.modules.meal.model.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoodItemSummaryResponse {

    private final Long id;
    private final String foodName;
    /** per_100g 热量（kcal），无数据时为 null */
    private final BigDecimal caloriesPer100g;
}
