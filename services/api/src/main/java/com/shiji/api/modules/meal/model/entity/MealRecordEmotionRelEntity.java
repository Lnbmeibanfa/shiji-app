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

@Getter
@Setter
@Entity
@Table(name = "meal_record_emotion_rel")
public class MealRecordEmotionRelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_record_id", nullable = false)
    private Long mealRecordId;

    @Column(name = "emotion_tag_id", nullable = false)
    private Long emotionTagId;

    @Column(name = "is_primary", nullable = false)
    private Integer isPrimary;

    @Column(name = "emotion_intensity")
    private Integer emotionIntensity;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
