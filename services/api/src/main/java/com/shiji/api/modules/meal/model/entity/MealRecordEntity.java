package com.shiji.api.modules.meal.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meal_record")
public class MealRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "meal_type", nullable = false, length = 32)
    private String mealType;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "primary_emotion_code", length = 32)
    private String primaryEmotionCode;

    @Column(length = 1024)
    private String note;

    @Column(name = "record_method", nullable = false, length = 32)
    private String recordMethod;

    @Column(name = "completion_status", nullable = false, length = 32)
    private String completionStatus;

    @Column(name = "recognition_status", nullable = false, length = 32)
    private String recognitionStatus;

    @Column(name = "total_estimated_calories", precision = 10, scale = 2)
    private BigDecimal totalEstimatedCalories;

    @Column(name = "total_estimated_protein", precision = 10, scale = 2)
    private BigDecimal totalEstimatedProtein;

    @Column(name = "total_estimated_fat", precision = 10, scale = 2)
    private BigDecimal totalEstimatedFat;

    @Column(name = "total_estimated_carb", precision = 10, scale = 2)
    private BigDecimal totalEstimatedCarb;

    @Column(name = "visibility_status", nullable = false)
    private Integer visibilityStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
