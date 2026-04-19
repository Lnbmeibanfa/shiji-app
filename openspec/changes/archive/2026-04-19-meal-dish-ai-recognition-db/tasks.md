## 1. 标准菜品层（DDL 与持久化模型）

- [x] 1.1 落地 `dish`、`dish_alias`、`dish_food_item_rel` 的建表 SQL（与 `services/api` 资源目录约定一致），并校对字符集、唯一约束与外键策略
- [x] 1.2 新增或对齐 JPA 实体、Repository 与枚举（`dish_source_type`、`dish_kind` 等），与现有 `food_item` 建模风格一致
- [x] 1.3 为运营/种子数据预留最小导入路径（可选脚本或文档说明），验证「可无 `dish_food_item_rel` 的 `dish`」可入库

## 2. 扩展 meal_record（餐级菜品命中）

- [x] 2.1 确认或迁移 `meal_record` 上 `dish_id`、`dish_name_snapshot`、`dish_match_source`、`dish_match_confidence` 列及 `dish` 外键
- [x] 2.2 更新 `MealRecord` 实体与保存/查询 DTO，保存时写入餐级快照字段并校验 `dish_id` 存在性（若传入）
- [x] 2.3 补充或更新接口契约（OpenAPI/文档或集成测试），覆盖「仅快照、无 `dish_id`」等边界

## 3. AI 识别过程层（DDL 与写入路径）

- [x] 3.1 落地 `meal_recognition_result`、`meal_recognition_item` 建表 SQL 及与 `meal_record` 的关联、索引
- [x] 3.2 实现识别结果持久化服务：写入主结果与明细行（模式、命中菜品、置信度、`raw_ai_response`、状态等）
- [x] 3.3 定义并实现「过程态 → 用户确认 → `meal_food_item` 终态」的事务或调用顺序，避免与既有热量求和规则冲突

## 4. 端到端链路（识别 → 回填 → 保存）

- [x] 4.1 调整识别编排：优先解析 `dish`，未命中则拆解 `food_item`，并同步写入 `meal_recognition_*`
- [x] 4.2 命中 `dish` 且存在 `dish_food_item_rel` 时，支持生成候选 `meal_food_item`（自动生成或待用户确认，按产品选择实现）
- [x] 4.3 Flutter/客户端：对齐 `record-meal-ai-flow` 增量（命中菜品展示、stub 与未来真实 AI 路径），并回归保存闸门与 `recordMethod` 行为
- [x] 4.4 补充集成测试或契约测试：覆盖命中菜品、fallback 多食物、识别失败留痕、历史 `dish_name_snapshot` 不因改名而丢失
