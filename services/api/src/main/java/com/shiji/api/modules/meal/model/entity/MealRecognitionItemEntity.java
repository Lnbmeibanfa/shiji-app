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

/** AI 识别过程拆解出的候选食物行。 */
@Getter
@Setter
@Entity
@Table(name = "meal_recognition_item")
public class MealRecognitionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recognition_result_id", nullable = false)
    private Long recognitionResultId;

    @Column(name = "food_item_id")
    private Long foodItemId;

    @Column(name = "food_name_snapshot", nullable = false, length = 128)
    private String foodNameSnapshot;

    @Column(name = "category_code_snapshot", length = 64)
    private String categoryCodeSnapshot;

    @Column(name = "recognition_confidence", precision = 5, scale = 4)
    private BigDecimal recognitionConfidence;

    @Column(name = "estimated_weight_g", precision = 10, scale = 2)
    private BigDecimal estimatedWeightG;

    @Column(name = "display_unit", nullable = false, length = 32)
    private String displayUnit;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
