package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealFoodItemRequest {

    private Long foodItemId;

    @NotBlank
    private String foodNameSnapshot;

    private String categoryCodeSnapshot;

    @NotBlank
    private String recognitionSource;

    private BigDecimal recognitionConfidence;

    private BigDecimal estimatedWeightG;

    private BigDecimal estimatedVolumeMl;

    private BigDecimal estimatedCount;

    @NotBlank
    private String displayUnit;

    private BigDecimal estimatedCalories;

    private BigDecimal estimatedProtein;

    private BigDecimal estimatedFat;

    private BigDecimal estimatedCarb;

    private String nutritionCalcBasis;

    @NotNull
    private Integer sortOrder;
}
