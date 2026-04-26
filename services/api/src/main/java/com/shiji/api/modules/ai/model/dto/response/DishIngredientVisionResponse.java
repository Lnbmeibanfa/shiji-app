package com.shiji.api.modules.ai.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record DishIngredientVisionResponse(String requestId, Recognition recognition) {

    @Builder
    public record Recognition(
            String schemaVersion,
            Dish dish,
            List<Ingredient> ingredients,
            DishRejection dishRejection,
            Map<String, Object> modelMeta) {}

    @Builder
    public record Dish(String dishId, double confidence, DishMatch match) {}

    @Builder
    public record DishMatch(String via, String aliasId) {}

    @Builder
    public record Ingredient(
            String ingredientId,
            double confidence,
            @JsonProperty(required = false) String foodName,
            @JsonProperty(required = false) String defaultUnit,
            @JsonProperty(required = false) Double caloriesPer100g) {}

    @Builder
    public record DishRejection(String code, String detail) {}
}
