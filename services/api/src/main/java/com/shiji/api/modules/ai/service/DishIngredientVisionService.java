package com.shiji.api.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiji.api.modules.ai.config.DashScopeProperties;
import com.shiji.api.modules.ai.model.dto.AiErrorCode;
import com.shiji.api.modules.ai.model.dto.response.DishIngredientVisionResponse;
import com.shiji.api.modules.ai.service.exception.AiBusinessException;
import com.shiji.api.modules.meal.model.entity.DishAliasEntity;
import com.shiji.api.modules.meal.model.entity.DishEntity;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import com.shiji.api.modules.meal.repository.FoodNutritionRepository;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DishIngredientVisionService {

    private static final int MAX_INGREDIENTS = 10;

    private final DashScopeProperties properties;
    private final DishIngredientVisionPromptService promptService;
    private final DashScopeVisionClient visionClient;
    private final VisionImagePreprocessor preprocessor;
    private final DishIngredientVocabProvider vocabProvider;
    private final FoodNutritionRepository foodNutritionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DishIngredientVisionResponse recognize(String imageBase64) {
        if (!StringUtils.hasText(imageBase64)) {
            throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID);
        }
        byte[] sourceBytes = decodeBase64(imageBase64);
        return recognizeFromBytes(sourceBytes, UUID.randomUUID().toString());
    }

    /**
     * 从原始图片字节识别；{@code requestId} 由调用方指定（如异步任务预生成，与落库一致）。
     */
    public DishIngredientVisionResponse recognizeFromBytes(byte[] sourceBytes, String requestId) {
        if (sourceBytes == null || sourceBytes.length == 0) {
            throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID);
        }
        if (!StringUtils.hasText(requestId)) {
            throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID);
        }
        VisionImagePreprocessor.ProcessedImage processed = preprocessor.process(sourceBytes);
        String prompt = promptService.buildPrompt();
        String rawContent = visionClient.inferJson(prompt, processed.bytes(), processed.mimeType());
        DashScopeVisionRawResult raw = parseRaw(rawContent);

        String outcome = normalize(raw.outcome());
        if ("NOT_FOOD".equals(outcome)) {
            throw new AiBusinessException(AiErrorCode.NOT_FOOD_IMAGE);
        }
        if ("UNRECOGNIZABLE".equals(outcome)) {
            throw new AiBusinessException(AiErrorCode.UNRECOGNIZABLE_IMAGE);
        }

        MappingContext context = MappingContext.from(vocabProvider.loadSnapshot());
        double threshold = properties.getVision().getConfidenceThreshold();
        DishMappingResult dishResult = mapDish(raw.dishCandidates(), context, threshold);
        List<DishIngredientVisionResponse.Ingredient> ingredients =
                mapIngredients(raw.ingredients(), context, threshold);

        Map<String, Object> modelMeta = new HashMap<>();
        modelMeta.put("model", properties.getVision().getModel());
        modelMeta.put("prompt_version", "dish-ingredient-vision-v1");
        modelMeta.put("origin_width", processed.originalWidth());
        modelMeta.put("origin_height", processed.originalHeight());
        modelMeta.put("processed_width", processed.processedWidth());
        modelMeta.put("processed_height", processed.processedHeight());
        modelMeta.put("origin_sha256", processed.originalSha256());

        return DishIngredientVisionResponse.builder()
                .requestId(requestId)
                .recognition(
                        DishIngredientVisionResponse.Recognition.builder()
                                .schemaVersion("1.0")
                                .dish(dishResult.dish())
                                .dishRejection(dishResult.rejection())
                                .ingredients(ingredients)
                                .modelMeta(modelMeta)
                                .build())
                .build();
    }

    private byte[] decodeBase64(String input) {
        String raw = input.trim();
        int comma = raw.indexOf(',');
        if (raw.startsWith("data:") && comma > 0) {
            raw = raw.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(raw);
        } catch (IllegalArgumentException e) {
            throw new AiBusinessException(AiErrorCode.VISION_REQUEST_INVALID, e);
        }
    }

    private DashScopeVisionRawResult parseRaw(String rawContent) {
        try {
            JsonNode node = objectMapper.readTree(rawContent);
            if (!node.isObject()) {
                throw new AiBusinessException(AiErrorCode.MODEL_OUTPUT_INVALID);
            }
            String outcome = node.path("outcome").asText("OK");
            List<DashScopeVisionRawResult.Candidate> dishes =
                    parseCandidates(node.path("dishCandidates"));
            List<DashScopeVisionRawResult.Candidate> ingredients =
                    parseCandidates(node.path("ingredients"));
            return new DashScopeVisionRawResult(outcome, dishes, ingredients);
        } catch (AiBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new AiBusinessException(AiErrorCode.MODEL_OUTPUT_INVALID, e);
        }
    }

    private List<DashScopeVisionRawResult.Candidate> parseCandidates(JsonNode node) {
        List<DashScopeVisionRawResult.Candidate> list = new ArrayList<>();
        if (!node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            String name = item.path("name").asText(null);
            if (!StringUtils.hasText(name)) {
                continue;
            }
            double confidence = item.path("confidence").asDouble(-1);
            if (confidence < 0 || confidence > 1) {
                continue;
            }
            list.add(new DashScopeVisionRawResult.Candidate(name.trim(), confidence));
        }
        return list;
    }

    private DishMappingResult mapDish(
            List<DashScopeVisionRawResult.Candidate> dishCandidates,
            MappingContext ctx,
            double threshold) {
        if (dishCandidates == null || dishCandidates.isEmpty()) {
            return DishMappingResult.rejected("NO_CATALOG_MATCH");
        }
        List<DashScopeVisionRawResult.Candidate> sorted =
                dishCandidates.stream()
                        .sorted(Comparator.comparing(DashScopeVisionRawResult.Candidate::confidence).reversed())
                        .toList();
        DashScopeVisionRawResult.Candidate first = sorted.getFirst();
        if (sorted.size() > 1 && Math.abs(first.confidence() - sorted.get(1).confidence()) < 0.05d) {
            return DishMappingResult.rejected("AMBIGUOUS");
        }
        if (first.confidence() < threshold) {
            return DishMappingResult.rejected("LOW_CONFIDENCE");
        }
        MatchResult match = ctx.matchDish(first.name());
        if (match == null) {
            return DishMappingResult.rejected("NO_CATALOG_MATCH");
        }
        DishIngredientVisionResponse.Dish dish =
                DishIngredientVisionResponse.Dish.builder()
                        .dishId(String.valueOf(match.dishId()))
                        .confidence(first.confidence())
                        .match(
                                DishIngredientVisionResponse.DishMatch.builder()
                                        .via(match.aliasId() == null ? "dish" : "alias")
                                        .aliasId(match.aliasId() == null ? null : String.valueOf(match.aliasId()))
                                        .build())
                        .build();
        return new DishMappingResult(dish, null);
    }

    private List<DishIngredientVisionResponse.Ingredient> mapIngredients(
            List<DashScopeVisionRawResult.Candidate> ingredients, MappingContext ctx, double threshold) {
        if (ingredients == null) {
            return List.of();
        }
        List<MatchedIngredient> matched =
                ingredients.stream()
                        .filter(c -> c.confidence() != null && c.confidence() >= threshold)
                        .map(c -> ctx.matchIngredientCandidate(c.name(), c.confidence()))
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(MatchedIngredient::confidence).reversed())
                        .limit(MAX_INGREDIENTS)
                        .toList();
        if (matched.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = new HashSet<>();
        for (MatchedIngredient m : matched) {
            ids.add(m.item().getId());
        }
        Map<Long, Double> kcalByFoodId = loadCaloriesPer100g(ids);
        List<DishIngredientVisionResponse.Ingredient> out = new ArrayList<>();
        for (MatchedIngredient m : matched) {
            long id = m.item().getId();
            out.add(
                    DishIngredientVisionResponse.Ingredient.builder()
                            .ingredientId(String.valueOf(id))
                            .confidence(m.confidence())
                            .foodName(m.item().getFoodName())
                            .defaultUnit(m.item().getDefaultUnit())
                            .caloriesPer100g(kcalByFoodId.get(id))
                            .build());
        }
        return out;
    }

    private Map<Long, Double> loadCaloriesPer100g(Set<Long> foodItemIds) {
        if (foodItemIds.isEmpty()) {
            return Map.of();
        }
        List<FoodNutritionEntity> rows =
                foodNutritionRepository.findAllByNutrientBasisAndFoodItemIdIn("per_100g", foodItemIds);
        Map<Long, FoodNutritionEntity> best = new HashMap<>();
        for (FoodNutritionEntity n : rows) {
            FoodNutritionEntity prev = best.get(n.getFoodItemId());
            if (prev == null || n.getVersionNo() > prev.getVersionNo()) {
                best.put(n.getFoodItemId(), n);
            }
        }
        Map<Long, Double> out = new HashMap<>();
        for (Map.Entry<Long, FoodNutritionEntity> e : best.entrySet()) {
            if (e.getValue().getCalories() != null) {
                out.put(e.getKey(), e.getValue().getCalories().doubleValue());
            }
        }
        return out;
    }

    private record MatchedIngredient(FoodItemEntity item, double confidence) {}

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record DishMappingResult(
            DishIngredientVisionResponse.Dish dish, DishIngredientVisionResponse.DishRejection rejection) {
        private static DishMappingResult rejected(String code) {
            return new DishMappingResult(
                    null, DishIngredientVisionResponse.DishRejection.builder().code(code).detail(code).build());
        }
    }

    private record MatchResult(Long dishId, Long aliasId) {}

    private record MappingContext(
            Map<String, DishEntity> dishByName,
            Map<String, List<DishAliasEntity>> aliasByName,
            Map<String, FoodItemEntity> ingredientByName) {
        static MappingContext from(DishIngredientVocabProvider.Snapshot snapshot) {
            Map<String, DishEntity> dishes = new HashMap<>();
            for (DishEntity d : snapshot.dishes()) {
                dishes.put(normalizeKey(d.getDishName()), d);
            }
            Map<String, List<DishAliasEntity>> aliases = new HashMap<>();
            for (DishAliasEntity alias : snapshot.aliases()) {
                aliases.computeIfAbsent(normalizeKey(alias.getAliasName()), k -> new ArrayList<>()).add(alias);
            }
            Map<String, FoodItemEntity> ingredients = new HashMap<>();
            for (FoodItemEntity f : snapshot.ingredients()) {
                ingredients.put(normalizeKey(f.getFoodName()), f);
            }
            return new MappingContext(dishes, aliases, ingredients);
        }

        MatchResult matchDish(String value) {
            String key = normalizeKey(value);
            DishEntity byDish = dishByName.get(key);
            if (byDish != null) {
                return new MatchResult(byDish.getId(), null);
            }
            List<DishAliasEntity> aliases = aliasByName.get(key);
            if (aliases == null || aliases.isEmpty()) {
                return null;
            }
            DishAliasEntity selected = aliases.stream().min(Comparator.comparing(DishAliasEntity::getId)).orElse(null);
            return selected == null ? null : new MatchResult(selected.getDishId(), selected.getId());
        }

        MatchedIngredient matchIngredientCandidate(String name, double confidence) {
            FoodItemEntity item = ingredientByName.get(normalizeKey(name));
            if (item == null) {
                return null;
            }
            return new MatchedIngredient(item, confidence);
        }
    }

    private static String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
