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

/** 与 {@code food_nutrition} 表对应；供 JPA 建表及可选写入。 */
@Getter
@Setter
@Entity
@Table(name = "food_nutrition")
public class FoodNutritionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_item_id", nullable = false)
    private Long foodItemId;

    @Column(name = "nutrient_basis", nullable = false, length = 32)
    private String nutrientBasis;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(precision = 10, scale = 2)
    private BigDecimal fat;

    @Column(precision = 10, scale = 2)
    private BigDecimal carbohydrate;

    @Column(precision = 10, scale = 2)
    private BigDecimal fiber;

    @Column(precision = 10, scale = 2)
    private BigDecimal sugar;

    @Column(precision = 10, scale = 2)
    private BigDecimal sodium;

    @Column(name = "data_source", length = 64)
    private String dataSource;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
