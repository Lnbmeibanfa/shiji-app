-- 遗留 `user_session` 可能无 `user_agent` 列，与 `UserSessionEntity` 对齐时执行一次（已存在则忽略报错）。
ALTER TABLE user_session
  ADD COLUMN user_agent VARCHAR(255) NULL AFTER login_ip;
