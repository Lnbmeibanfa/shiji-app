CREATE TABLE IF NOT EXISTS meal_record_emotion_rel (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  meal_record_id BIGINT NOT NULL COMMENT '饮食记录ID',
  emotion_tag_id BIGINT NOT NULL COMMENT '情绪标签ID',

  is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主情绪：0否 1是',
  emotion_intensity TINYINT NULL COMMENT '情绪强度：1-5',
  source_type VARCHAR(32) NOT NULL DEFAULT 'user_selected' COMMENT '来源：user_selected | ai_inferred | system_derived',
  remark VARCHAR(255) NULL COMMENT '备注',

  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

  PRIMARY KEY (id),
  UNIQUE KEY uk_meal_emotion (meal_record_id, emotion_tag_id),
  KEY idx_emotion_tag_id (emotion_tag_id),
  KEY idx_meal_primary (meal_record_id, is_primary),

  CONSTRAINT fk_meal_record_emotion_rel_meal FOREIGN KEY (meal_record_id) REFERENCES meal_record (id),
  CONSTRAINT fk_meal_record_emotion_rel_tag FOREIGN KEY (emotion_tag_id) REFERENCES emotion_tag (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='饮食记录与情绪感受标签关联表';