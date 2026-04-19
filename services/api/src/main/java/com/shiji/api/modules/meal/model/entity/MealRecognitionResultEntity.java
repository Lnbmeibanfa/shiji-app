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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 一餐对应的 AI 识别过程主结果（与 {@link MealFoodItemEntity} 最终确认行分层）。 */
@Getter
@Setter
@Entity
@Table(name = "meal_recognition_result")
public class MealRecognitionResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_record_id", nullable = false, unique = true)
    private Long mealRecordId;

    @Column(name = "recognition_mode", nullable = false, length = 32)
    private String recognitionMode;

    @Column(name = "result_source", nullable = false, length = 32)
    private String resultSource;

    @Column(name = "matched_dish_id")
    private Long matchedDishId;

    @Column(name = "matched_dish_name", length = 128)
    private String matchedDishName;

    @Column(name = "overall_confidence", precision = 5, scale = 4)
    private BigDecimal overallConfidence;

    @Column(name = "need_user_confirm", nullable = false)
    private Integer needUserConfirm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_ai_response")
    private String rawAiResponse;

    @Column(name = "model_name", length = 64)
    private String modelName;

    @Column(name = "prompt_version", length = 32)
    private String promptVersion;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
