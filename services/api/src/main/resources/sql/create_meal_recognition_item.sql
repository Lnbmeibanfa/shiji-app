CREATE TABLE shiji.meal_recognition_item
(
    id                       BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    recognition_result_id    BIGINT                                   NOT NULL COMMENT '识别结果ID',
    food_item_id             BIGINT                                   NULL COMMENT '标准基础食物ID',
    food_name_snapshot       VARCHAR(128)                             NOT NULL COMMENT '识别出的食物名称快照',
    category_code_snapshot   VARCHAR(64)                              NULL COMMENT '识别出的分类快照',
    recognition_confidence   DECIMAL(5,4)                             NULL COMMENT '识别置信度',
    estimated_weight_g       DECIMAL(10,2)                            NULL COMMENT '估算重量(g)',
    display_unit             VARCHAR(32) DEFAULT 'g'                  NOT NULL COMMENT '展示单位',
    source_type              VARCHAR(32) DEFAULT 'ai'                 NOT NULL COMMENT '来源：ai | fallback | manual',
    sort_order               INT         DEFAULT 0                    NOT NULL COMMENT '排序值',
    created_at               DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL COMMENT '创建时间',
    updated_at               DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    CONSTRAINT fk_mri_result
        FOREIGN KEY (recognition_result_id) REFERENCES shiji.meal_recognition_result (id),
    CONSTRAINT fk_mri_food_item
        FOREIGN KEY (food_item_id) REFERENCES shiji.food_item (id)
)
    COMMENT '餐次AI识别结果明细表'
    COLLATE = utf8mb4_unicode_ci;