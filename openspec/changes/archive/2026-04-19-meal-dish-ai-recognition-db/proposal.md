## Why

食迹需要在不推翻现有「基础食物项」链路的前提下，补齐**标准菜品层**与 **AI 识别过程留痕**，以支持「图片优先命中菜品，未命中再拆解基础食物」的架构，并为 AI 建议、周报、外卖菜品库与用户历史命中预留结构化扩展位。当前 `food_item` + `food_nutrition` + `meal_food_item` + `meal_record` 整体可用，本次为**增量升级**而非重写模型。

## What Changes

- **保留并复用**：`food_item`、`food_nutrition`、`meal_record`、`meal_food_item`、`meal_record_image`、`meal_record_emotion_rel`、`file_asset`、`emotion_tag` 等现有表及语义（`food_item` 为标准字典，`meal_food_item` 为每餐业务快照）。
- **新增标准菜品层**：`dish`（与外卖共表，MVP 不拆 `external_dish`）、`dish_alias`、`dish_food_item_rel`；通过 `dish_source_type`、`dish_kind` 等区分来源与品类；**不**在本阶段强制新增 `dish_nutrition`（可由 `dish_food_item_rel` + `food_nutrition` 聚合，后续按需补表）。
- **扩展 `meal_record`**：增加菜品命中与餐级快照相关字段（如 `dish_id`、`dish_name_snapshot`、`dish_match_source`、`dish_match_confidence`），避免仅依赖 `dish_id` 丢失历史名称与来源。
- **新增 AI 识别过程层**：`meal_recognition_result`、`meal_recognition_item`，与「最终确认的 `meal_food_item`」分层；过程表记录识别模式、命中菜品、置信度、原始响应、是否需要确认、状态与失败原因等。
- **快照分层**：餐级快照在 `meal_record`；食物项名称/分类等快照在 `meal_food_item`；AI 原始过程与候选明细在 `meal_recognition_*`。
- **实施顺序建议**：先菜品层表 → 再扩展 `meal_record` → 再识别过程表 → 最后调整应用链路（先识别 dish、回填、再决定是否生成 `meal_food_item` 并落识别结果）。

## Capabilities

### New Capabilities

- `meal-dish-catalog-schema`：定义标准菜品层数据模型与约束（`dish`、`dish_alias`、`dish_food_item_rel`），包括与 `food_item` 的可选关联、来源/品类枚举语义，及与外卖共表的演进边界。
- `meal-ai-recognition-schema`：定义 AI 识别过程持久化模型（`meal_recognition_result`、`meal_recognition_item`），明确与 `meal_record` / `meal_food_item` 的职责分界（过程态 vs 业务确认态）。

### Modified Capabilities

- `meal-record-save`：在数据与（若同步实现的）API 层扩展餐级菜品命中与快照字段的约定，并与识别过程表、最终 `meal_food_item` 写入规则一致。
- `record-meal-ai-flow`：在产品与实现演进上对齐「先 dish 后基础食物 fallback」的识别链路；识别过程与最终列表的落库分层（具体 UI 状态机可在实现阶段按增量 delta 细化）。

## Impact

- **数据库**：新增多张表及 `meal_record` 列；需迁移脚本/SQL 与 JPA 实体（若后端已建模）对齐。
- **后端 API**：保存一餐、查询餐详情、未来识别回调等可能需携带或返回 `dish_id`、快照与识别元数据（以 spec 与实现为准）。
- **客户端**：记录饮食 / AI 流程在接入真实识别后需区分「识别过程」与「用户确认后保存」；与现有 `meal-record-save`、`record-meal-ai-flow` 规范协同演进。
- **依赖**：不改变 `food_item` 作为标准字典的核心地位；后续外卖大规模商品库可在 `dish` 上继续演进或再拆表。
