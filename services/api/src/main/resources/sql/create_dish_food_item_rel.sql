CREATE TABLE shiji.dish_food_item_rel
(
    id                  BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    dish_id             BIGINT                                   NOT NULL COMMENT '菜品ID',
    food_item_id        BIGINT                                   NOT NULL COMMENT '基础食物ID',
    role_type           VARCHAR(32) DEFAULT 'main'               NOT NULL COMMENT '角色：main | side | topping | seasoning | base',
    default_weight_g    DECIMAL(10,2)                            NULL COMMENT '默认重量(g)',
    weight_ratio        DECIMAL(5,2)                             NULL COMMENT '占比，0~100',
    is_optional         TINYINT     DEFAULT 0                    NOT NULL COMMENT '是否可选：0否 1是',
    sort_order          INT         DEFAULT 0                    NOT NULL COMMENT '排序值',
    created_at          DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL COMMENT '创建时间',
    updated_at          DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    CONSTRAINT uk_dish_food_item UNIQUE (dish_id, food_item_id),
    CONSTRAINT fk_dfir_dish
        FOREIGN KEY (dish_id) REFERENCES shiji.dish (id),
    CONSTRAINT fk_dfir_food_item
        FOREIGN KEY (food_item_id) REFERENCES shiji.food_item (id)
)
    COMMENT '菜品与基础食物关联表'
    COLLATE = utf8mb4_unicode_ci;