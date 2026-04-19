CREATE TABLE shiji.meal_recognition_result
(
    id                       BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    meal_record_id           BIGINT                                   NOT NULL COMMENT '饮食记录ID',
    recognition_mode         VARCHAR(32) DEFAULT 'dish_first'         NOT NULL COMMENT '识别模式：dish_first | food_only | manual',
    result_source            VARCHAR(32)                              NOT NULL COMMENT '结果来源：dish_match | food_recognition | mixed | manual_edit',
    matched_dish_id          BIGINT                                   NULL COMMENT '命中的标准菜品ID',
    matched_dish_name        VARCHAR(128)                             NULL COMMENT '命中的菜品名称快照',
    overall_confidence       DECIMAL(5,4)                             NULL COMMENT '整体置信度',
    need_user_confirm        TINYINT     DEFAULT 0                    NOT NULL COMMENT '是否需要用户确认：0否 1是',
    raw_ai_response          JSON                                     NULL COMMENT 'AI原始响应',
    model_name               VARCHAR(64)                              NULL COMMENT '模型名称',
    prompt_version           VARCHAR(32)                              NULL COMMENT 'Prompt版本',
    status                   VARCHAR(32) DEFAULT 'success'            NOT NULL COMMENT '状态：success | partial_success | failed | review_needed',
    failure_reason           VARCHAR(255)                             NULL COMMENT '失败原因',
    created_at               DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL COMMENT '创建时间',
    updated_at               DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    CONSTRAINT uk_mrr_meal_record UNIQUE (meal_record_id),
    CONSTRAINT fk_mrr_meal_record
        FOREIGN KEY (meal_record_id) REFERENCES shiji.meal_record (id),
    CONSTRAINT fk_mrr_dish
        FOREIGN KEY (matched_dish_id) REFERENCES shiji.dish (id)
)
    COMMENT '餐次AI识别结果主表'
    COLLATE = utf8mb4_unicode_ci;