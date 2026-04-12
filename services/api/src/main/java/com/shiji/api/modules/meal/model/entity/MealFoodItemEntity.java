package com.shiji.api.modules.meal.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meal_food_item")
public class MealFoodItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_record_id", nullable = false)
    private Long mealRecordId;

    @Column(name = "food_item_id")
    private Long foodItemId;

    @Column(name = "food_name_snapshot", nullable = false, length = 128)
    private String foodNameSnapshot;

    @Column(name = "category_code_snapshot", length = 64)
    private String categoryCodeSnapshot;

    @Column(name = "recognition_source", nullable = false, length = 32)
    private String recognitionSource;

    @Column(name = "recognition_confidence", precision = 5, scale = 4)
    private BigDecimal recognitionConfidence;

    @Column(name = "estimated_weight_g", precision = 10, scale = 2)
    private BigDecimal estimatedWeightG;

    @Column(name = "estimated_volume_ml", precision = 10, scale = 2)
    private BigDecimal estimatedVolumeMl;

    @Column(name = "estimated_count", precision = 10, scale = 2)
    private BigDecimal estimatedCount;

    @Column(name = "display_unit", nullable = false, length = 32)
    private String displayUnit;

    @Column(name = "estimated_calories", precision = 10, scale = 2)
    private BigDecimal estimatedCalories;

    @Column(name = "estimated_protein", precision = 10, scale = 2)
    private BigDecimal estimatedProtein;

    @Column(name = "estimated_fat", precision = 10, scale = 2)
    private BigDecimal estimatedFat;

    @Column(name = "estimated_carb", precision = 10, scale = 2)
    private BigDecimal estimatedCarb;

    @Column(name = "nutrition_calc_basis", length = 32)
    private String nutritionCalcBasis;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
