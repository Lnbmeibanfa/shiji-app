package com.shiji.api.modules.meal.model.dto.request;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 与 {@code meal_recognition_result} / {@code meal_recognition_item} 对齐的一次识别过程写入体。 */
@Getter
@Setter
public class MealRecognitionPersistRequest {

    private String recognitionMode;

    private String resultSource;

    private Long matchedDishId;

    private String matchedDishName;

    private BigDecimal overallConfidence;

    /** 0 或 1 */
    private Integer needUserConfirm;

    /** 原始 JSON 字符串 */
    private String rawAiResponse;

    private String modelName;

    private String promptVersion;

    private String status;

    private String failureReason;

    @Valid
    private List<MealRecognitionItemPersistRequest> items = new ArrayList<>();
}
