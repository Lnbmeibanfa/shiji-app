package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.MealRecognitionItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecognitionItemRepository extends JpaRepository<MealRecognitionItemEntity, Long> {

    List<MealRecognitionItemEntity> findByRecognitionResultIdOrderBySortOrderAsc(Long recognitionResultId);
}
