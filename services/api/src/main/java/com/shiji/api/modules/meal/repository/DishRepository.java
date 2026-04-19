package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<DishEntity, Long> {

    boolean existsByIdAndEdibleStatus(Long id, Integer edibleStatus);
}
