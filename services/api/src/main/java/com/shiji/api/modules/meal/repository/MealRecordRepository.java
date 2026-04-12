package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.MealRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecordRepository extends JpaRepository<MealRecordEntity, Long> {}
