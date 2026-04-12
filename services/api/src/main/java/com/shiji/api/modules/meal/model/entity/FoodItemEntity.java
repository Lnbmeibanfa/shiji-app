package com.shiji.api.modules.meal.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 标准食物表；与 {@code meal_food_item.food_item_id} 可选外键对应。 */
@Getter
@Setter
@Entity
@Table(name = "food_item")
public class FoodItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_code", nullable = false, length = 64)
    private String foodCode;

    @Column(name = "food_name", nullable = false, length = 128)
    private String foodName;

    @Column(name = "food_alias", length = 255)
    private String foodAlias;

    @Column(name = "category_code", length = 64)
    private String categoryCode;

    @Column(name = "brand_name", length = 128)
    private String brandName;

    @Column(name = "default_unit", nullable = false, length = 32)
    private String defaultUnit;

    @Column(name = "edible_status", nullable = false)
    private Integer edibleStatus;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
