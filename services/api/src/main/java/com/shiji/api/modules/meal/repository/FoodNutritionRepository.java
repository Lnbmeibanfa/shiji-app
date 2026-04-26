package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.FoodNutritionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodNutritionRepository extends JpaRepository<FoodNutritionEntity, Long> {

    Optional<FoodNutritionEntity> findFirstByFoodItemIdOrderByVersionNoDesc(Long foodItemId);

    List<FoodNutritionEntity> findAllByNutrientBasisAndFoodItemIdIn(
            String nutrientBasis, Collection<Long> foodItemIds);
}
