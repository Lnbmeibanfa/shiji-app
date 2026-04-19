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

/** 标准菜品与基础食物项的组成关系。 */
@Getter
@Setter
@Entity
@Table(name = "dish_food_item_rel")
public class DishFoodItemRelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "food_item_id", nullable = false)
    private Long foodItemId;

    @Column(name = "role_type", nullable = false, length = 32)
    private String roleType;

    @Column(name = "default_weight_g", precision = 10, scale = 2)
    private BigDecimal defaultWeightG;

    @Column(name = "weight_ratio", precision = 5, scale = 2)
    private BigDecimal weightRatio;

    @Column(name = "is_optional", nullable = false)
    private Integer isOptional;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
