package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMealRecordRequest {

    @NotBlank
    private String mealType;

    @NotNull
    private LocalDateTime recordedAt;

    private String note;

    /** 默认 manual */
    private String recordMethod;

    /** 默认 completed */
    private String completionStatus;

    /** 默认 skipped */
    private String recognitionStatus;

    @Valid
    private List<MealFoodItemRequest> foodItems = new ArrayList<>();

    @Valid
    private List<MealFoodImageRequest> images = new ArrayList<>();

    @Valid
    private List<MealEmotionRequest> emotions = new ArrayList<>();
}
