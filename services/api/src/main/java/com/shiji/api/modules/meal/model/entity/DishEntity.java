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

/** 标准菜品 / 商品目录；与 {@code meal_record.dish_id}、识别结果等可选外键对应。 */
@Getter
@Setter
@Entity
@Table(name = "dish")
public class DishEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_code", nullable = false, length = 64, unique = true)
    private String dishCode;

    @Column(name = "dish_name", nullable = false, length = 128)
    private String dishName;

    @Column(name = "dish_alias", length = 255)
    private String dishAlias;

    @Column(name = "dish_kind", nullable = false, length = 32)
    private String dishKind;

    @Column(name = "category_code", length = 64)
    private String categoryCode;

    @Column(name = "cuisine_type", length = 64)
    private String cuisineType;

    @Column(name = "brand_name", length = 128)
    private String brandName;

    @Column(name = "dish_source_type", nullable = false, length = 32)
    private String dishSourceType;

    @Column(name = "support_food_split", nullable = false)
    private Integer supportFoodSplit;

    @Column(name = "default_unit", nullable = false, length = 32)
    private String defaultUnit;

    @Column(name = "default_weight_g", precision = 10, scale = 2)
    private BigDecimal defaultWeightG;

    @Column(name = "edible_status", nullable = false)
    private Integer edibleStatus;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
