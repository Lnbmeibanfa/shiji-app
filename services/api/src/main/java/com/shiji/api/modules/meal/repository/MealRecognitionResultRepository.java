package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.MealRecognitionResultEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecognitionResultRepository extends JpaRepository<MealRecognitionResultEntity, Long> {

    Optional<MealRecognitionResultEntity> findByMealRecordId(Long mealRecordId);
}
