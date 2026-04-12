package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodNutritionRepository extends JpaRepository<FoodNutritionEntity, Long> {}
