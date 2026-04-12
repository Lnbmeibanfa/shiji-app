package com.shiji.api.modules.meal.repository;

import com.shiji.api.modules.meal.model.entity.FoodItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodItemRepository extends JpaRepository<FoodItemEntity, Long> {

    @Query(
            value =
                    "SELECT fi.id, fi.food_name, fn.calories FROM food_item fi "
                            + "LEFT JOIN food_nutrition fn ON fn.food_item_id = fi.id "
                            + "AND fn.nutrient_basis = 'per_100g' AND fn.version_no = 1 "
                            + "WHERE fi.edible_status = 1 "
                            + "AND (LENGTH(TRIM(:q)) = 0 OR fi.food_name LIKE CONCAT('%', :q, '%') "
                            + "OR (fi.food_alias IS NOT NULL AND fi.food_alias LIKE CONCAT('%', :q, '%'))) "
                            + "ORDER BY fi.id ASC",
            countQuery =
                    "SELECT COUNT(fi.id) FROM food_item fi "
                            + "WHERE fi.edible_status = 1 "
                            + "AND (LENGTH(TRIM(:q)) = 0 OR fi.food_name LIKE CONCAT('%', :q, '%') "
                            + "OR (fi.food_alias IS NOT NULL AND fi.food_alias LIKE CONCAT('%', :q, '%')))",
            nativeQuery = true)
    Page<Object[]> searchWithNutrition(@Param("q") String q, Pageable pageable);
}
