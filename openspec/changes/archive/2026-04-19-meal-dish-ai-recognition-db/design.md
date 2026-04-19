## Context

食迹已具备可用的「标准基础食物」链路：`food_item`（平台字典）、`food_nutrition`、`meal_record`（一餐）、`meal_food_item`（该餐下的食物行快照，含名称/分类/重量/营养/来源/置信度等）。为支持图片 AI **优先命中用户感知层的「菜品」**，未命中再 **fallback 拆解为多个 `food_item`**，并区分 **识别过程态** 与 **用户确认后的业务结果**，需要在现有模型上做增量扩展，而非推翻重建。

约束包括：MVP 阶段外卖菜品与标准菜品 **共表 `dish`**，通过 `dish_source_type`、`dish_kind` 等区分；**不**在本阶段引入 `dish_nutrition`（由 `dish_food_item_rel` + `food_nutrition` 聚合即可）；快照 **分层**：餐级在 `meal_record`，食物行在 `meal_food_item`，AI 原始过程在 `meal_recognition_*`。

## Goals / Non-Goals

**Goals:**

- 在数据模型上建立 **标准菜品层**：`dish`、`dish_alias`、`dish_food_item_rel`，表达菜品与基础食物的可选组成关系。
- 扩展 `meal_record`，持久化 **菜品命中** 与 **餐级名称/来源/置信度快照**，保证历史可追溯（不只存 `dish_id`）。
- 新增 **AI 识别过程层**：`meal_recognition_result`、`meal_recognition_item`，与 `meal_food_item`（最终确认结果）职责分离。
- 明确 **实施顺序**：先菜品表 → 再 `meal_record` 扩展 → 再识别过程表 → 最后调整应用与 API 链路。

**Non-Goals:**

- 本变更 **不** 强制落地完整外卖商品库或独立 `external_dish` 表。
- **不** 强制新增 `dish_nutrition` 物化表。
- **不** 在本设计文档中规定具体 AI 模型、prompt 版本或第三方供应商细节（仅在识别结果表中留字段位）。

## Decisions

### D1：菜品与基础食物分层的职责

- **`food_item`**：平台标准基础食物/食材字典（米饭、鸡蛋、鸡胸肉等），不是某一餐的记录。
- **`dish`**：平台标准菜品/商品感知层（番茄炒蛋、奶茶、套餐等），供识别优先命中；可与 `food_item` **无**关联（如难拆解的套餐、包装饮品）。
- **`meal_food_item`**：某一 `meal_record` 下 **最终采用** 的食物行快照；**不**被 `meal_record` 单独承载全部快照职责。

**备选**：仅用 `meal_food_item` 表达菜品 — **否决**：缺少跨用户复用的标准菜品目录与别名命中面。

### D2：外卖与标准菜品共表

- 使用 **`dish.dish_source_type`**（如 `system_standard`、`takeout_candidate`、`merchant_imported`、`ai_generated`、`manual`）与 **`dish_kind`**（如 `dish`、`drink`、`dessert`、`package_meal`）区分来源与类型。
- 大规模外卖库成熟后再评估是否拆 `external_dish`。

**备选**：早期拆 `external_dish` — **否决**：MVP 复杂度与迁移成本过高。

### D3：识别过程与业务结果分表

- **`meal_recognition_result` / `meal_recognition_item`**：一次识别任务的 **过程态**（模式、命中菜品、原始响应、模型与 prompt 版本、是否需确认、成功/部分成功/失败等）。
- **`meal_food_item`**：用户确认或业务规则采纳后的 **终态** 食物行。

**备选**：全过程只写 `meal_food_item` 注释字段 — **否决**：过程字段膨胀、难以做分析与回放。

### D4：`dish` 与 `food_item` 的关联

- 通过 **`dish_food_item_rel`** 表达组成；字段可包含 `role_type`、`default_weight_g`、`weight_ratio`、`is_optional`、`sort_order` 等。
- **不** 要求每个 `dish` 都有关联行。

### D5：营养聚合

- 菜品级营养 **默认** 由 `dish_food_item_rel` → `food_item` → `food_nutrition` **聚合**；若未来有性能或强快照需求，再引入 `dish_nutrition`。

## Risks / Trade-offs

- **[Risk] `dish` 与外卖共表导致字段语义膨胀** → **Mitigation**：严格使用 `dish_source_type` / `dish_kind` 与文档约定；查询与索引按来源分区考虑。
- **[Risk] 识别过程表写入量大** → **Mitigation**：可按保留策略归档或汇总；MVP 先全量留痕便于调模型。
- **[Risk] `meal_record` 与识别表数据不一致** → **Mitigation**：同一事务写入或明确最终一致顺序；以 `meal_food_item` 为展示与统计真源。
- **[Trade-off]** 不建 `dish_nutrition` 降低冗余，但高频聚合时可能增加查询成本 — 后续按需物化。

## Migration Plan

1. **阶段一（DDL）**：创建 `dish`、`dish_alias`、`dish_food_item_rel`；确保 `dish_code` 等唯一约束与字符集（如 `utf8mb4_unicode_ci`）一致。
2. **阶段二（DDL）**：为 `meal_record` 增加 `dish_id`、`dish_name_snapshot`、`dish_match_source`、`dish_match_confidence` 及外键（若库中尚无）；已有列则校验与代码一致。
3. **阶段三（DDL）**：创建 `meal_recognition_result`、`meal_recognition_item`，建立与 `meal_record` 的关联及必要索引。
4. **阶段四（应用）**：识别服务先写 `meal_recognition_*`，确认后写/更新 `meal_record` 与 `meal_food_item`；API 与 Flutter 按 spec delta 演进。
5. **Rollback**：回滚顺序与上线相反；新表可保留空表；新增列若可空则降级应用兼容旧客户端。

## Open Questions

- 识别结果与一餐的 **1:1 还是 1:N**（多次重试）是否需在 `meal_recognition_result` 用版本或 `attempt` 字段明确（实现阶段定）。
- `dish_match_source` 与 `meal_recognition_result.result_source` 的 **枚举对齐** 是否合并为单一字典（避免双处漂移）。
