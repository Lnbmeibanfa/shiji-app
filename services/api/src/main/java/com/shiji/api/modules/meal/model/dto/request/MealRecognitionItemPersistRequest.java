package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealRecognitionItemPersistRequest {

    private Long foodItemId;

    @NotBlank
    private String foodNameSnapshot;

    private String categoryCodeSnapshot;

    private BigDecimal recognitionConfidence;

    private BigDecimal estimatedWeightG;

    @NotBlank
    private String displayUnit;

    /** 默认 ai */
    private String sourceType;

    @NotNull
    private Integer sortOrder;
}
