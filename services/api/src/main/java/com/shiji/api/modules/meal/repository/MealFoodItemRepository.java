package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.MealFoodItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealFoodItemRepository extends JpaRepository<MealFoodItemEntity, Long> {

    List<MealFoodItemEntity> findByMealRecordIdOrderBySortOrderAsc(Long mealRecordId);
}
