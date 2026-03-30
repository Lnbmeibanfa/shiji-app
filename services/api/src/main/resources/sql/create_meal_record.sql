-- ---------------------------------------------------------------------------
-- 饮食记录（一餐一条）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meal_record (
  id BIGINT NOT NULL AUTO_INCREMENT,

  user_id BIGINT NOT NULL COMMENT '用户ID',

  file_id BIGINT NOT NULL COMMENT '主图文件ID，关联 file_asset.id',

  meal_type VARCHAR(32) NULL COMMENT '餐别：breakfast | lunch | dinner | snack',

  recorded_at DATETIME(6) NOT NULL COMMENT '用餐时间（用户选择）',

  note VARCHAR(1024) NULL COMMENT '备注',

  deleted_at DATETIME(6) NULL COMMENT '软删除时间',

  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,

  PRIMARY KEY (id),

  KEY idx_meal_user_recorded (user_id, recorded_at),
  KEY idx_meal_file (file_id),

  CONSTRAINT fk_meal_user FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT fk_meal_file FOREIGN KEY (file_id) REFERENCES file_asset (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='饮食记录表';