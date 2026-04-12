CREATE TABLE IF NOT EXISTS food_nutrition (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  food_item_id BIGINT NOT NULL COMMENT '食物ID',
  nutrient_basis VARCHAR(32) NOT NULL DEFAULT 'per_100g' COMMENT '营养基准：per_100g | per_100ml | per_piece',

  calories DECIMAL(10,2) NOT NULL COMMENT '热量(kcal)',
  protein DECIMAL(10,2) NULL COMMENT '蛋白质(g)',
  fat DECIMAL(10,2) NULL COMMENT '脂肪(g)',
  carbohydrate DECIMAL(10,2) NULL COMMENT '碳水(g)',
  fiber DECIMAL(10,2) NULL COMMENT '膳食纤维(g)',
  sugar DECIMAL(10,2) NULL COMMENT '糖(g)',
  sodium DECIMAL(10,2) NULL COMMENT '钠(mg)',

  data_source VARCHAR(64) NULL COMMENT '营养数据来源',
  version_no INT NOT NULL DEFAULT 1 COMMENT '版本号',

  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

  PRIMARY KEY (id),
  UNIQUE KEY uk_food_basis_version (food_item_id, nutrient_basis, version_no),
  KEY idx_food_item_id (food_item_id),

  CONSTRAINT fk_food_nutrition_food FOREIGN KEY (food_item_id) REFERENCES food_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='食物营养表';