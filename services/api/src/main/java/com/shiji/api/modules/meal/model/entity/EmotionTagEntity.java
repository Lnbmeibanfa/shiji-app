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
@Table(name = "emotion_tag")
public class EmotionTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emotion_code", nullable = false, length = 64)
    private String emotionCode;

    @Column(name = "emotion_name", nullable = false, length = 64)
    private String emotionName;

    @Column(name = "emotion_category", nullable = false, length = 32)
    private String emotionCategory;

    @Column(name = "emotion_status", nullable = false)
    private Integer emotionStatus;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_system", nullable = false)
    private Integer isSystem;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
