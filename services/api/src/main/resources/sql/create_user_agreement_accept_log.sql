CREATE TABLE `user_agreement_accept_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID，首次登录前可为空',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号，登录前记录可用',
  `agreement_type` VARCHAR(32) NOT NULL COMMENT '协议类型：user_agreement/privacy_policy',
  `agreement_version` VARCHAR(32) NOT NULL COMMENT '协议版本号',
  `accepted` TINYINT NOT NULL DEFAULT 1 COMMENT '是否同意：1是',
  `accept_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同意时间',
  `request_ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  `device_id` VARCHAR(128) DEFAULT NULL COMMENT '设备ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议同意记录表';