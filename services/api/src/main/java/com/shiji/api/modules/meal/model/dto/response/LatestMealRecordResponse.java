package com.shiji.api.modules.meal.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LatestMealRecordResponse {

    private final Long mealRecordId;
    private final String mealType;
    private final LocalDateTime recordedAt;
    private final BigDecimal totalEstimatedCalories;
    private final Mood mood;

    @Getter
    @Builder
    public static class Mood {
        private final String emotionCode;
        private final String emotionName;
    }
}
