-- 可选：运营向 MySQL 导入标准菜品的最小示例。
-- 「奶茶」等可无 dish_food_item_rel，仅插入 dish 一行即可。
-- 执行前请按需调整库名、时区与重复执行策略（INSERT IGNORE / ON DUPLICATE KEY UPDATE）。

-- INSERT INTO dish (dish_code, dish_name, dish_kind, dish_source_type, support_food_split, default_unit, edible_status)
-- VALUES ('demo_milk_tea_001', '珍珠奶茶', 'drink', 'system_standard', 0, 'cup', 1);
