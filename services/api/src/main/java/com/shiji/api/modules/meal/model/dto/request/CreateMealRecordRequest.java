package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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

    /** 命中的标准菜品；可与仅快照字段并存 */
    private Long dishId;

    private String dishNameSnapshot;

    private String dishMatchSource;

    private BigDecimal dishMatchConfidence;

    /**
     * 为 true 且 {@link #foodItems} 为空时，按 {@link #dishId} 查询 {@code dish_food_item_rel} 自动生成食物行（用于服务端拆解）。
     */
    private Boolean expandDishToFoodItems;

    /** 可选：持久化本次 AI 识别过程（与最终 foodItems 分层） */
    @Valid
    private MealRecognitionPersistRequest recognition;

    @Valid
    private List<MealFoodItemRequest> foodItems = new ArrayList<>();

    @Valid
    private List<MealFoodImageRequest> images = new ArrayList<>();

    @Valid
    private List<MealEmotionRequest> emotions = new ArrayList<>();
}
