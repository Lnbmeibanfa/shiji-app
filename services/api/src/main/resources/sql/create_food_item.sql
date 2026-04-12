CREATE TABLE IF NOT EXISTS food_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  food_code VARCHAR(64) NOT NULL COMMENT '食物编码',
  food_name VARCHAR(128) NOT NULL COMMENT '标准食物名称',
  food_alias VARCHAR(255) NULL COMMENT '别名集合，可逗号分隔，MVP先简单存',
  category_code VARCHAR(64) NULL COMMENT '食物分类编码：staple | meat | vegetable | drink | dessert',
  brand_name VARCHAR(128) NULL COMMENT '品牌名，通用食物可为空',

  default_unit VARCHAR(32) NOT NULL DEFAULT 'g' COMMENT '默认单位：g | ml | piece | bowl',
  edible_status TINYINT NOT NULL DEFAULT 1 COMMENT '是否可用：1可用 0停用',

  source_type VARCHAR(32) NOT NULL DEFAULT 'system' COMMENT '来源：system | imported | ai_generated | manual',
  remark VARCHAR(255) NULL COMMENT '备注',

  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

  PRIMARY KEY (id),
  UNIQUE KEY uk_food_code (food_code),
  KEY idx_food_name (food_name),
  KEY idx_food_category (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='标准食物表';