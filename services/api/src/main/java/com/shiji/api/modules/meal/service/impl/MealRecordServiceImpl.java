package com.shiji.api.modules.meal.service.impl;

import com.shiji.api.modules.file.model.entity.FileAssetEntity;
import com.shiji.api.modules.file.repository.FileAssetRepository;
import com.shiji.api.modules.meal.model.dto.MealErrorCode;
import com.shiji.api.modules.meal.model.dto.request.CreateMealRecordRequest;
import com.shiji.api.modules.meal.model.dto.request.MealEmotionRequest;
import com.shiji.api.modules.meal.model.dto.request.MealFoodImageRequest;
import com.shiji.api.modules.meal.model.dto.request.MealFoodItemRequest;
import com.shiji.api.modules.meal.model.dto.response.CreateMealRecordResponse;
import com.shiji.api.modules.meal.model.entity.EmotionTagEntity;
import com.shiji.api.modules.meal.model.entity.MealFoodItemEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordEmotionRelEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordEntity;
import com.shiji.api.modules.meal.model.entity.MealRecordImageEntity;
import com.shiji.api.modules.meal.repository.EmotionTagRepository;
import com.shiji.api.modules.meal.repository.FoodItemRepository;
import com.shiji.api.modules.meal.repository.MealFoodItemRepository;
import com.shiji.api.modules.meal.repository.MealRecordEmotionRelRepository;
import com.shiji.api.modules.meal.repository.MealRecordImageRepository;
import com.shiji.api.modules.meal.repository.MealRecordRepository;
import com.shiji.api.modules.meal.service.MealRecordService;
import com.shiji.api.modules.meal.service.exception.MealBusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        for (MealFoodItemRequest item : request.getFoodItems()) {
            if (item.getFoodItemId() != null && !foodItemRepository.existsById(item.getFoodItemId())) {
                throw new MealBusinessException(MealErrorCode.MEAL_REQUEST_INVALID);
            }
        }

        BigDecimal totalCalories = sumFoodField(request.getFoodItems(), MealFoodItemRequest::getEstimatedCalories);
        BigDecimal totalProtein = sumFoodField(request.getFoodItems(), MealFoodItemRequest::getEstimatedProtein);
        BigDecimal totalFat = sumFoodField(request.getFoodItems(), MealFoodItemRequest::getEstimatedFat);
        BigDecimal totalCarb = sumFoodField(request.getFoodItems(), MealFoodItemRequest::getEstimatedCarb);

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
        meal.setVisibilityStatus(1);
        meal.setDeletedAt(null);
        meal.setCreatedAt(now);
        meal.setUpdatedAt(now);

        mealRecordRepository.save(meal);
        Long mealId = meal.getId();

        for (MealFoodItemRequest item : request.getFoodItems()) {
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

        return CreateMealRecordResponse.builder().mealRecordId(mealId).build();
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
