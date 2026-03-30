-- ---------------------------------------------------------------------------
-- 文件资产（支持：后端代理上传 + 二期 STS 直传）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS file_asset (
  id BIGINT NOT NULL AUTO_INCREMENT,

  user_id BIGINT NOT NULL COMMENT '归属用户',

  storage_provider VARCHAR(32) NOT NULL DEFAULT 'aliyun_oss' COMMENT '存储提供方，如 aliyun_oss',
  bucket VARCHAR(128) NOT NULL COMMENT 'Bucket 名',
  object_key VARCHAR(1024) NOT NULL COMMENT 'OSS Object Key（真实存储路径）',

  object_key_hash CHAR(64) NOT NULL COMMENT 'bucket + object_key 的 SHA-256，用于唯一索引',

  url VARCHAR(2048) NULL COMMENT '访问 URL（可冗余，域名变更可重算）',
  thumbnail_url VARCHAR(2048) NULL COMMENT '缩略图 URL（异步生成后回填）',

  file_name VARCHAR(512) NULL COMMENT '原始文件名',
  content_type VARCHAR(128) NULL COMMENT 'MIME 类型，如 image/jpeg',
  file_size BIGINT NULL COMMENT '文件大小（字节）',

  etag VARCHAR(128) NULL COMMENT 'OSS ETag，用于校验',
  content_hash CHAR(64) NULL COMMENT '文件内容 SHA-256，用于去重',

  width INT NULL COMMENT '图片宽度',
  height INT NULL COMMENT '图片高度',

  upload_source VARCHAR(32) NOT NULL DEFAULT 'backend_proxy'
    COMMENT '上传来源：backend_proxy | client_direct',

  status VARCHAR(32) NOT NULL
    COMMENT '状态：pending | uploaded | bound | abandoned | deleted',

  biz_type VARCHAR(32) NOT NULL DEFAULT 'meal_photo'
    COMMENT '业务类型：meal_photo 等',

  uploaded_at DATETIME(6) NULL COMMENT '上传完成时间',
  bound_at DATETIME(6) NULL COMMENT '绑定业务时间',

  expire_at DATETIME(6) NULL COMMENT '过期时间（用于清理未绑定文件）',

  ext JSON NULL COMMENT '扩展字段（EXIF / 设备信息等）',

  deleted_at DATETIME(6) NULL COMMENT '软删除时间',

  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY uk_file_object_hash (object_key_hash),

  KEY idx_file_user_status (user_id, status),
  KEY idx_file_user_created (user_id, created_at),
  KEY idx_file_pending_expire (status, expire_at),

  CONSTRAINT fk_file_user FOREIGN KEY (user_id) REFERENCES user (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='文件资产表';