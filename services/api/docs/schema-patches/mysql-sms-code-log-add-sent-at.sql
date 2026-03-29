-- 与 `SmsCodeLogEntity` 对齐：频控与排序依赖 `sent_at`。
-- 若启动或调用发码接口报错：Unknown column 'sent_at' in 'where clause'，在目标库执行本脚本一次。

ALTER TABLE sms_code_log
  ADD COLUMN sent_at DATETIME(6) NULL COMMENT '发送时间' AFTER device_id;

-- 已有数据：用创建时间回填；若无 created_at 列请改为 NOW(6)
UPDATE sms_code_log
SET sent_at = COALESCE(created_at, updated_at, NOW(6))
WHERE sent_at IS NULL;

ALTER TABLE sms_code_log
  MODIFY COLUMN sent_at DATETIME(6) NOT NULL;
