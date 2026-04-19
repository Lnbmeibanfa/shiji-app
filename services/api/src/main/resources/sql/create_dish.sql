CREATE TABLE shiji.dish
(
    id                  BIGINT AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    dish_code           VARCHAR(64)                              NOT NULL COMMENT '菜品编码',
    dish_name           VARCHAR(128)                             NOT NULL COMMENT '标准菜品名称',
    dish_alias          VARCHAR(255)                             NULL COMMENT '别名集合，MVP先简单冗余存储',
    dish_kind           VARCHAR(32)  DEFAULT 'dish'             NOT NULL COMMENT '类型：dish | drink | dessert | package_meal',
    category_code       VARCHAR(64)                              NULL COMMENT '菜品分类编码：chinese_food | western_food | milk_tea | coffee | snack | dessert',
    cuisine_type        VARCHAR(64)                              NULL COMMENT '菜系/风格：sichuan | cantonese | japanese | korean | western',
    brand_name          VARCHAR(128)                             NULL COMMENT '品牌名，外卖/连锁商品可有值',
    dish_source_type    VARCHAR(32)  DEFAULT 'system_standard'  NOT NULL COMMENT '来源：system_standard | takeout_candidate | merchant_imported | ai_generated | manual',
    support_food_split  TINYINT      DEFAULT 1                  NOT NULL COMMENT '是否支持拆解为food_item：1是 0否',
    default_unit        VARCHAR(32)  DEFAULT 'portion'          NOT NULL COMMENT '默认展示单位：portion | cup | bottle | bowl',
    default_weight_g    DECIMAL(10,2)                            NULL COMMENT '默认整份重量(g)',
    edible_status       TINYINT      DEFAULT 1                  NOT NULL COMMENT '状态：1可用 0停用',
    remark              VARCHAR(255)                             NULL COMMENT '备注',
    created_at          DATETIME(6)  DEFAULT CURRENT_TIMESTAMP(6) NOT NULL COMMENT '创建时间',
    updated_at          DATETIME(6)  DEFAULT CURRENT_TIMESTAMP(6) NOT NULL ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    CONSTRAINT uk_dish_code UNIQUE (dish_code)
)
    COMMENT '标准菜品表'
    COLLATE = utf8mb4_unicode_ci;