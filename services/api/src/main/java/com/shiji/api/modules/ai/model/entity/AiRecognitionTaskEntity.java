package com.shiji.api.modules.ai.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "ai_recognition_task")
public class AiRecognitionTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, length = 64, unique = true)
    private String taskId;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "scene", nullable = false, length = 32)
    private String scene = "meal_record";

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "error_code")
    private Integer errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "model_name", length = 64)
    private String modelName;

    @Column(name = "prompt_version", length = 32)
    private String promptVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_json")
    private String resultJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
