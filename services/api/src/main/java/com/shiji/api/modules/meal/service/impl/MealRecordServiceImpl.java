package com.shiji.api.modules.meal.service.impl;

import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import com.shiji.api.modules.meal.model.dto.MealErrorCode;
import com.shiji.api.modules.meal.model.dto.request.CreateMealRecordRequest;
import com.shiji.api.modules.meal.model.dto.request.MealEmotionRequest;
import com.shiji.api.modules.meal.model.dto.request.MealFoodImageRequest;
import com.shiji.api.modules.meal.model.dto.request.MealFoodItemRequest;
import com.shiji.api.modules.meal.model.dto.request.MealRecognitionItemPersistRequest;
import com.shiji.api.modules.meal.model.dto.request.MealRecognitionPersistRequest;
import com.shiji.api.modules.meal.model.dto.response.CreateMealRecordResponse;
import com.shiji.api.modules.meal.model.entity.DishFoodItemRelEntity;
import com.shiji.api.modules.meal.model.entity.EmotionTagEntity;
import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import com.shiji.api.modules.meal.model.entity.MealFoodItemEntity;
import com.shiji.api.modules.meal.model.entity.MealRecognitionItemEntity;
import com.shiji.api.modules.meal.model.entity.MealRecognitionResultEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordEmotionRelEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordImageEntity;
import com.shiji.api.modules.meal.repository.DishFoodItemRelRepository;
import com.shiji.api.modules.meal.repository.DishRepository;
import com.shiji.api.modules.meal.repository.EmotionTagRepository;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.repository.FoodNutritionRepository;
import com.shiji.api.modules.meal.repository.MealFoodItemRepository;
import com.shiji.api.modules.meal.repository.MealRecognitionItemRepository;
import com.shiji.api.modules.meal.repository.MealRecognitionResultRepository;
import com.shiji.api.modules.meal.repository.MealRecordEmotionRelRepository;
import com.shiji.api.modules.meal.repository.MealRecordImageRepository;
import com.shiji.api.modules.meal.repository.MealRecordRepository;
import com.shiji.api.modules.meal.service.MealRecordService;
import com.shiji.api.modules.meal.service.exception.MealBusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MealRecordServiceImpl implements MealRecordService {

    /** 与 {@code meal_record.record_date} 推导一致：将请求体中的 {@code recordedAt} 视为该时区下的本地时间再取日历日。 */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String FILE_STATUS_UPLOADED = "uploaded";

    private final MealRecordRepository mealRecordRepository;
    private final MealFoodItemRepository mealFoodItemRepository;
    private final MealRecordImageRepository mealRecordImageRepository;
    private final MealRecordEmotionRelRepository mealRecordEmotionRelRepository;
    private final EmotionTagRepository emotionTagRepository;
    private final FileAssetRepository fileAssetRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodNutritionRepository foodNutritionRepository;
    private final DishRepository dishRepository;
    private final DishFoodItemRelRepository dishFoodItemRelRepository;
    private final MealRecognitionResultRepository mealRecognitionResultRepository;
    private final MealRecognitionItemRepository mealRecognitionItemRepository;

    @Override
    @Transactional
    public CreateMealRecordResponse create(long userId, CreateMealRecordRequest request) {
        assertNoDuplicateFileIds(request.getImages());
        assertNoDuplicateEmotionTagIds(request.getEmotions());

        for (MealFoodImageRequest image : request.getImages()) {
            validateFileForUser(userId, image.getFileId());
        }

        List<EmotionTagEntity> resolvedEmotions = resolveEmotionTags(request.getEmotions());
        String primaryEmotionCode = computePrimaryEmotionCode(resolvedEmotions);

        if (request.getDishId() != null) {
            assertDishUsable(request.getDishId());
        }

        List<MealFoodItemRequest> resolvedFoodItems = resolveFoodItems(request);

        for (MealFoodItemRequest item : resolvedFoodItems) {
            if (item.getFoodItemId() != null && !foodItemRepository.existsById(item.getFoodItemId())) {
                throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
            }
        }

        if (request.getRecognition() != null) {
            assertRecognitionRefs(request.getRecognition());
        }

        BigDecimal totalCalories = sumFoodField(resolvedFoodItems, MealFoodItemRequest::getEstimatedCalories);
        BigDecimal totalProtein = sumFoodField(resolvedFoodItems, MealFoodItemRequest::getEstimatedProtein);
        BigDecimal totalFat = sumFoodField(resolvedFoodItems, MealFoodItemRequest::getEstimatedFat);
        BigDecimal totalCarb = sumFoodField(resolvedFoodItems, MealFoodItemRequest::getEstimatedCarb);

        LocalDateTime now = LocalDateTime.now();
        MealRecordEntity meal = new MealRecordEntity();
        meal.setUserId(userId);
        meal.setMealType(request.getMealType());
        meal.setRecordedAt(request.getRecordedAt());
        meal.setRecordDate(request.getRecordedAt().atZone(BUSINESS_ZONE).toLocalDate());
        meal.setPrimaryEmotionCode(primaryEmotionCode);
        meal.setNote(request.getNote());
        meal.setRecordMethod(StringUtils.hasText(request.getRecordMethod()) ? request.getRecordMethod() : "manual");
        meal.setCompletionStatus(
                StringUtils.hasText(request.getCompletionStatus()) ? request.getCompletionStatus() : "completed");
        meal.setRecognitionStatus(
                StringUtils.hasText(request.getRecognitionStatus()) ? request.getRecognitionStatus() : "skipped");
        meal.setTotalEstimatedCalories(totalCalories);
        meal.setTotalEstimatedProtein(totalProtein);
        meal.setTotalEstimatedFat(totalFat);
        meal.setTotalEstimatedCarb(totalCarb);
        meal.setDishId(request.getDishId());
        meal.setDishNameSnapshot(request.getDishNameSnapshot());
        meal.setDishMatchSource(request.getDishMatchSource());
        meal.setDishMatchConfidence(request.getDishMatchConfidence());
        meal.setVisibilityStatus(1);
        meal.setDeletedAt(null);
        meal.setCreatedAt(now);
        meal.setUpdatedAt(now);

        mealRecordRepository.save(meal);
        Long mealId = meal.getId();

        for (MealFoodItemRequest item : resolvedFoodItems) {
            MealFoodItemEntity row = new MealFoodItemEntity();
            row.setMealRecordId(mealId);
            row.setFoodItemId(item.getFoodItemId());
            row.setFoodNameSnapshot(item.getFoodNameSnapshot());
            row.setCategoryCodeSnapshot(item.getCategoryCodeSnapshot());
            row.setRecognitionSource(item.getRecognitionSource());
            row.setRecognitionConfidence(item.getRecognitionConfidence());
            row.setEstimatedWeightG(item.getEstimatedWeightG());
            row.setEstimatedVolumeMl(item.getEstimatedVolumeMl());
            row.setEstimatedCount(item.getEstimatedCount());
            row.setDisplayUnit(item.getDisplayUnit());
            row.setEstimatedCalories(item.getEstimatedCalories());
            row.setEstimatedProtein(item.getEstimatedProtein());
            row.setEstimatedFat(item.getEstimatedFat());
            row.setEstimatedCarb(item.getEstimatedCarb());
            row.setNutritionCalcBasis(item.getNutritionCalcBasis());
            row.setSortOrder(item.getSortOrder());
            row.setIsDeleted(0);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            mealFoodItemRepository.save(row);
        }

        for (MealFoodImageRequest image : request.getImages()) {
            MealRecordImageEntity row = new MealRecordImageEntity();
            row.setMealRecordId(mealId);
            row.setFileId(image.getFileId());
            row.setIsPrimary(image.getIsPrimary());
            row.setSortOrder(image.getSortOrder());
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            mealRecordImageRepository.save(row);
        }

        EmotionTagEntity primaryTag = pickPrimaryTag(resolvedEmotions);
        for (MealEmotionRequest em : request.getEmotions()) {
            MealRecordEmotionRelEntity row = new MealRecordEmotionRelEntity();
            row.setMealRecordId(mealId);
            row.setEmotionTagId(em.getEmotionTagId());
            boolean isPrimary =
                    primaryTag != null && primaryTag.getId().equals(em.getEmotionTagId());
            row.setIsPrimary(isPrimary ? 1 : 0);
            row.setEmotionIntensity(em.getEmotionIntensity());
            row.setSourceType("user_selected");
            row.setRemark(em.getRemark());
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            mealRecordEmotionRelRepository.save(row);
        }

        if (request.getRecognition() != null) {
            persistRecognition(mealId, request.getRecognition(), now);
        }

        return CreateMealRecordResponse.builder().mealRecordId(mealId).build();
    }

    private void assertDishUsable(long dishId) {
        if (!dishRepository.existsByIdAndEdibleStatus(dishId, 1)) {
            throw new MealBusinessException(MealErrorCode.MEAL_DISH_NOT_FOUND);
        }
    }

    private void assertRecognitionRefs(MealRecognitionPersistRequest recognition) {
        if (recognition.getMatchedDishId() != null) {
            assertDishUsable(recognition.getMatchedDishId());
        }
        for (MealRecognitionItemPersistRequest line : recognition.getItems()) {
            if (line.getFoodItemId() != null && !foodItemRepository.existsById(line.getFoodItemId())) {
                throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
            }
        }
    }

    /**
     * 优先使用请求中的食物行；若为空且开启拆解，则按 {@code dish_food_item_rel} 生成候选行（同一事务内再写入
     * {@link MealFoodItemEntity}，保证与总热量求和一致）。
     */
    private List<MealFoodItemRequest> resolveFoodItems(CreateMealRecordRequest request) {
        List<MealFoodItemRequest> fromClient = request.getFoodItems();
        if (fromClient != null && !fromClient.isEmpty()) {
            return new ArrayList<>(fromClient);
        }
        if (Boolean.TRUE.equals(request.getExpandDishToFoodItems()) && request.getDishId() != null) {
            assertDishUsable(request.getDishId());
            List<MealFoodItemRequest> expanded = expandDishToFoodItems(request.getDishId());
            if (expanded.isEmpty()) {
                throw new MealBusinessException(MealErrorCode.MEAL_DISH_EXPAND_EMPTY);
            }
            return expanded;
        }
        return fromClient != null ? new ArrayList<>(fromClient) : new ArrayList<>();
    }

    private List<MealFoodItemRequest> expandDishToFoodItems(long dishId) {
        List<DishFoodItemRelEntity> rels = dishFoodItemRelRepository.findByDishIdOrderBySortOrderAscIdAsc(dishId);
        List<MealFoodItemRequest> out = new ArrayList<>();
        int order = 0;
        for (DishFoodItemRelEntity rel : rels) {
            FoodItemEntity fi = foodItemRepository
                    .findById(rel.getFoodItemId())
                    .orElseThrow(() -> new MealBusinessException(MealErrorCode.MEAL_DISH_EXPAND_EMPTY));
            MealFoodItemRequest row = new MealFoodItemRequest();
            row.setFoodItemId(fi.getId());
            row.setFoodNameSnapshot(fi.getFoodName());
            row.setCategoryCodeSnapshot(fi.getCategoryCode());
            row.setRecognitionSource("dish_expand");
            row.setDisplayUnit("g");
            row.setSortOrder(order++);
            BigDecimal weightG = rel.getDefaultWeightG() != null
                    ? rel.getDefaultWeightG()
                    : new BigDecimal("100");
            row.setEstimatedWeightG(weightG);
            applyPer100gNutritionEstimates(row, fi.getId(), weightG);
            out.add(row);
        }
        return out;
    }

    private void applyPer100gNutritionEstimates(MealFoodItemRequest row, long foodItemId, BigDecimal weightG) {
        Optional<FoodNutritionEntity> nOpt = foodNutritionRepository.findFirstByFoodItemIdOrderByVersionNoDesc(foodItemId);
        if (nOpt.isEmpty()) {
            return;
        }
        FoodNutritionEntity n = nOpt.get();
        if (!"per_100g".equals(n.getNutrientBasis())) {
            return;
        }
        BigDecimal factor = weightG.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
        if (n.getCalories() != null) {
            row.setEstimatedCalories(n.getCalories().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        }
        if (n.getProtein() != null) {
            row.setEstimatedProtein(n.getProtein().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        }
        if (n.getFat() != null) {
            row.setEstimatedFat(n.getFat().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        }
        if (n.getCarbohydrate() != null) {
            row.setEstimatedCarb(n.getCarbohydrate().multiply(factor).setScale(2, RoundingMode.HALF_UP));
        }
        row.setNutritionCalcBasis("food_db");
    }

    private void persistRecognition(long mealRecordId, MealRecognitionPersistRequest r, LocalDateTime now) {
        MealRecognitionResultEntity head = new MealRecognitionResultEntity();
        head.setMealRecordId(mealRecordId);
        head.setRecognitionMode(StringUtils.hasText(r.getRecognitionMode()) ? r.getRecognitionMode() : "dish_first");
        head.setResultSource(StringUtils.hasText(r.getResultSource()) ? r.getResultSource() : "food_recognition");
        head.setMatchedDishId(r.getMatchedDishId());
        head.setMatchedDishName(r.getMatchedDishName());
        head.setOverallConfidence(r.getOverallConfidence());
        head.setNeedUserConfirm(r.getNeedUserConfirm() != null ? r.getNeedUserConfirm() : 0);
        head.setRawAiResponse(r.getRawAiResponse());
        head.setModelName(r.getModelName());
        head.setPromptVersion(r.getPromptVersion());
        head.setStatus(StringUtils.hasText(r.getStatus()) ? r.getStatus() : "success");
        head.setFailureReason(r.getFailureReason());
        head.setCreatedAt(now);
        head.setUpdatedAt(now);
        mealRecognitionResultRepository.save(head);
        Long resultId = head.getId();

        for (MealRecognitionItemPersistRequest line : r.getItems()) {
            MealRecognitionItemEntity it = new MealRecognitionItemEntity();
            it.setRecognitionResultId(resultId);
            it.setFoodItemId(line.getFoodItemId());
            it.setFoodNameSnapshot(line.getFoodNameSnapshot());
            it.setCategoryCodeSnapshot(line.getCategoryCodeSnapshot());
            it.setRecognitionConfidence(line.getRecognitionConfidence());
            it.setEstimatedWeightG(line.getEstimatedWeightG());
            it.setDisplayUnit(line.getDisplayUnit());
            it.setSourceType(StringUtils.hasText(line.getSourceType()) ? line.getSourceType() : "ai");
            it.setSortOrder(line.getSortOrder());
            it.setCreatedAt(now);
            it.setUpdatedAt(now);
            mealRecognitionItemRepository.save(it);
        }
    }

    private static void assertNoDuplicateFileIds(List<MealFoodImageRequest> images) {
        Set<Long> seen = new HashSet<>();
        for (MealFoodImageRequest img : images) {
            if (!seen.add(img.getFileId())) {
                throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
            }
        }
    }

    private static void assertNoDuplicateEmotionTagIds(List<MealEmotionRequest> emotions) {
        Set<Long> seen = new HashSet<>();
        for (MealEmotionRequest em : emotions) {
            if (!seen.add(em.getEmotionTagId())) {
                throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
            }
        }
    }

    private void validateFileForUser(long userId, long fileId) {
        Optional<FileAssetEntity> mine = fileAssetRepository.findByIdAndUserId(fileId, userId);
        if (mine.isPresent()) {
            FileAssetEntity file = mine.get();
            if (!FILE_STATUS_UPLOADED.equals(file.getStatus()) || file.getDeletedAt() != null) {
                throw new MealBusinessException(MealErrorCode.MEAL_FILE_NOT_FOUND);
            }
            return;
        }
        if (fileAssetRepository.existsById(fileId)) {
            throw new MealBusinessException(MealErrorCode.MEAL_FILE_FORBIDDEN);
        }
        throw new MealBusinessException(MealErrorCode.MEAL_FILE_NOT_FOUND);
    }

    private List<EmotionTagEntity> resolveEmotionTags(List<MealEmotionRequest> emotions) {
        if (emotions.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = emotions.stream().map(MealEmotionRequest::getEmotionTagId).collect(Collectors.toSet());
        List<EmotionTagEntity> tags = emotionTagRepository.findAllById(ids);
        if (tags.size() != ids.size()) {
            throw new MealBusinessException(MealErrorCode.EMOTION_TAG_INVALID);
        }
        for (EmotionTagEntity tag : tags) {
            if (tag.getEmotionStatus() == null || tag.getEmotionStatus() != 1) {
                throw new MealBusinessException(MealErrorCode.EMOTION_TAG_INVALID);
            }
        }
        return tags;
    }

    private static String computePrimaryEmotionCode(List<EmotionTagEntity> tags) {
        EmotionTagEntity primary = pickPrimaryTag(tags);
        return primary != null ? primary.getEmotionCode() : null;
    }

    private static EmotionTagEntity pickPrimaryTag(List<EmotionTagEntity> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .min(Comparator.comparing(EmotionTagEntity::getSortOrder)
                        .thenComparing(EmotionTagEntity::getId))
                .orElse(null);
    }

    private static BigDecimal sumFoodField(
            List<MealFoodItemRequest> items, java.util.function.Function<MealFoodItemRequest, BigDecimal> getter) {
        BigDecimal sum = BigDecimal.ZERO;
        for (MealFoodItemRequest item : items) {
            BigDecimal v = getter.apply(item);
            if (v != null) {
                sum = sum.add(v);
            }
        }
        return sum;
    }
}
