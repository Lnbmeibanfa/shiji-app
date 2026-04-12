CREATE TABLE IF NOT EXISTS `meal_record_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `meal_record_id` bigint NOT NULL COMMENT '饮食记录ID',
  `file_id` bigint NOT NULL COMMENT '文件ID，关联 file_asset.id',
  `is_primary` tinyint NOT NULL DEFAULT 0 COMMENT '是否主图：0否 1是',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meal_file` (`meal_record_id`,`file_id`),
  KEY `idx_meal_primary` (`meal_record_id`,`is_primary`),
  KEY `idx_file_id` (`file_id`),
  CONSTRAINT `fk_meal_record_image_meal` FOREIGN KEY (`meal_record_id`) REFERENCES `meal_record` (`id`),
  CONSTRAINT `fk_meal_record_image_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食记录图片关联表';