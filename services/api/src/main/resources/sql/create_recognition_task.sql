CREATE TABLE `ai_recognition_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` VARCHAR(64) NOT NULL COMMENT '对外任务ID(UUID)',
  `request_id` VARCHAR(64) NOT NULL COMMENT '链路追踪ID，与识别结果 requestId 对齐',
  `user_id` BIGINT NOT NULL,
  `file_id` BIGINT NOT NULL,
  `scene` VARCHAR(32) NOT NULL DEFAULT 'meal_record',

  `status` VARCHAR(32) NOT NULL COMMENT 'pending|processing|success|failed',

  `error_code` INT DEFAULT NULL COMMENT '失败时业务错误码',
  `error_message` VARCHAR(255) DEFAULT NULL,

  `model_name` VARCHAR(64) DEFAULT NULL,
  `prompt_version` VARCHAR(32) DEFAULT NULL,

  `result_json` JSON DEFAULT NULL COMMENT '成功时 DishIngredientVisionResponse JSON',

  `created_at` DATETIME(6) NOT NULL,
  `started_at` DATETIME(6) DEFAULT NULL,
  `finished_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) NOT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_task_task_id` (`task_id`),
  KEY `idx_ai_task_user_created` (`user_id`, `created_at`),
  KEY `idx_ai_task_status_created` (`status`, `created_at`),
  KEY `idx_ai_task_file` (`file_id`),
  KEY `idx_ai_task_finished` (`finished_at`),
  CONSTRAINT `fk_ai_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_ai_task_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐图AI异步识别任务';
