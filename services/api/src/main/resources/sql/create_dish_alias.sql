CREATE TABLE shiji.dish_alias
(
    id              BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    dish_id         BIGINT                                   NOT NULL COMMENT '标准菜品ID',
    alias_name      VARCHAR(128)                             NOT NULL COMMENT '别名',
    alias_type      VARCHAR(32) DEFAULT 'synonym'            NOT NULL COMMENT '别名类型：synonym | takeout_name | ocr_name | ai_name',
    created_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL COMMENT '创建时间',
    updated_at      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    CONSTRAINT uk_dish_alias_name UNIQUE (alias_name),
    CONSTRAINT fk_dish_alias_dish
        FOREIGN KEY (dish_id) REFERENCES shiji.dish (id)
)
    COMMENT '菜品别名表'
    COLLATE = utf8mb4_unicode_ci;