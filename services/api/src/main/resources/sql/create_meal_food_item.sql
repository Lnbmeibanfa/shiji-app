CREATE TABLE IF NOT EXISTS meal_food_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  meal_record_id BIGINT NOT NULL COMMENT '饮食记录ID',
  food_item_id BIGINT NULL COMMENT '标准食物ID，未匹配成功时可为空',

  food_name_snapshot VARCHAR(128) NOT NULL COMMENT '本次记录中的食物名称快照',
  category_code_snapshot VARCHAR(64) NULL COMMENT '本次记录中的食物分类快照',

  recognition_source VARCHAR(32) NOT NULL DEFAULT 'ai' COMMENT '来源：ai | user_manual | user_corrected',
  recognition_confidence DECIMAL(5,4) NULL COMMENT 'AI识别置信度，0~1',

  estimated_weight_g DECIMAL(10,2) NULL COMMENT '估算重量(g)',
  estimated_volume_ml DECIMAL(10,2) NULL COMMENT '估算体积(ml)',
  estimated_count DECIMAL(10,2) NULL COMMENT '估算数量（个/份）',
  display_unit VARCHAR(32) NOT NULL DEFAULT 'g' COMMENT '展示单位：g | ml | piece | bowl',

  estimated_calories DECIMAL(10,2) NULL COMMENT '该食物项估算热量(kcal)',
  estimated_protein DECIMAL(10,2) NULL COMMENT '该食物项估算蛋白质(g)',
  estimated_fat DECIMAL(10,2) NULL COMMENT '该食物项估算脂肪(g)',
  estimated_carb DECIMAL(10,2) NULL COMMENT '该食物项估算碳水(g)',

  nutrition_calc_basis VARCHAR(32) NULL COMMENT '营养计算依据：food_db | ai_direct | user_manual',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，控制展示顺序',

  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',

  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

  PRIMARY KEY (id),
  KEY idx_meal_record_id (meal_record_id),
  KEY idx_food_item_id (food_item_id),
  KEY idx_meal_sort (meal_record_id, sort_order),

  CONSTRAINT fk_meal_food_item_meal FOREIGN KEY (meal_record_id) REFERENCES meal_record (id),
  CONSTRAINT fk_meal_food_item_food FOREIGN KEY (food_item_id) REFERENCES food_item (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='饮食记录食物项表（一餐多食物）';