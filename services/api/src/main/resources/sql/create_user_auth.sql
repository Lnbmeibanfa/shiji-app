CREATE TABLE `user_auth` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `auth_type` VARCHAR(32) NOT NULL COMMENT '认证类型：mobile/apple/email/google/wechat',
  `auth_key` VARCHAR(128) NOT NULL COMMENT '认证标识，如手机号/邮箱/第三方openId',
  `auth_key_masked` VARCHAR(128) DEFAULT NULL COMMENT '脱敏后的认证标识，如 138****1234',
  `auth_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 2解绑 3停用',
  `is_primary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主认证方式：0否 1是',
  `verified_at` DATETIME DEFAULT NULL COMMENT '认证通过时间',
  `bound_at` DATETIME DEFAULT NULL COMMENT '绑定时间',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息，如第三方unionid、payload等',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_auth_type_key` (`auth_type`, `auth_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_auth_type` (`user_id`, `auth_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证凭证表';
