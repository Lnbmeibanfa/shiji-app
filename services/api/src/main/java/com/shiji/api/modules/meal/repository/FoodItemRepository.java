package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository<FoodItemEntity, Long> {}
