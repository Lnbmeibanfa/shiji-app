-- 开发环境：按 `SmsCodeLogEntity` 重建 `sms_code_log`（会删除该表已有数据）。
-- 适用于旧表结构与 JPA 实体不一致（如存在 biz_type、status 为 tinyint、列名为 error_count 等）导致插入/查询异常时。

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sms_code_log;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sms_code_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  phone VARCHAR(32) NOT NULL,
  code_hash VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  expire_at DATETIME(6) NOT NULL,
  verify_fail_count INT NOT NULL,
  request_ip VARCHAR(64),
  device_id VARCHAR(128),
  sent_at DATETIME(6) NOT NULL,
  verified_at DATETIME(6),
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_sms_phone (phone),
  KEY idx_sms_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
