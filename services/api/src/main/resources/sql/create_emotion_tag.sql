CREATE TABLE IF NOT EXISTS emotion_tag (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  emotion_code VARCHAR(64) NOT NULL COMMENT '情绪编码，唯一标识',
  emotion_name VARCHAR(64) NOT NULL COMMENT '情绪名称',
  emotion_category VARCHAR(32) NOT NULL COMMENT '情绪分类：emotion | feeling | motivation',
  emotion_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  is_system TINYINT NOT NULL DEFAULT 1 COMMENT '是否系统标签：1是 0否',
  remark VARCHAR(255) NULL COMMENT '备注',

  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

  PRIMARY KEY (id),
  UNIQUE KEY uk_emotion_code (emotion_code),
  KEY idx_emotion_category_status (emotion_category, emotion_status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='情绪感受标签定义表';