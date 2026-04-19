# 饮食记录保存 API（meal-record-save）— 变更增量

本文件为 `openspec/specs/meal-record-save/spec.md` 的增量规范：在「保存一餐」语义上扩展 **餐级菜品命中与快照**，与标准菜品层及识别过程层对齐。

---

## ADDED Requirements

### Requirement: 餐级菜品命中与名称快照可持久化

当业务或识别流程为一餐确定了标准菜品命中关系时，系统 SHALL 在 `meal_record` 上持久化 `dish_id`（若已解析为标准菜品）以及 **餐级** `dish_name_snapshot`、`dish_match_source`、`dish_match_confidence`（若适用），以保证历史展示与审计不依赖仅关联 ID。

系统 SHALL NOT 仅依赖 `dish_id` 作为唯一餐级展示依据而不提供名称快照能力（允许快照与当前 `dish.dish_name` 因后续改名而不一致）。

#### Scenario: 命中标准菜品写入快照

- **WHEN** 保存一餐时已知命中的 `dish_id` 与当时展示名
- **THEN** 持久化后的 `meal_record` SHALL 包含 `dish_id` 与 `dish_name_snapshot`（及适用的来源与置信度字段）

#### Scenario: 菜品改名不覆盖历史餐次

- **WHEN** 标准库中该 `dish` 后续修改了标准名称
- **THEN** 历史 `meal_record` SHALL 仍 SHALL 能通过 `dish_name_snapshot` 还原用户当时看到的名称

### Requirement: 保存一餐与识别过程、最终食物行职责分界

保存一餐接口在扩展后 SHALL 继续以 `meal_food_item` 作为 **最终确认** 的食物行真源；AI 识别 **过程态** 数据 SHALL 由 `meal_recognition_result` / `meal_recognition_item` 承担（若本次流程产生），SHALL NOT 仅因存在识别结果而跳过对 `meal_food_item` 的约定校验（除非产品明确允许空食物行的草稿态，且现有规范另行定义）。

#### Scenario: 过程表与业务行并存

- **WHEN** 同一次保存同时存在识别过程记录与最终食物行
- **THEN** 识别过程 SHALL 落在识别过程表；展示与汇总热量等 SHALL 以 `meal_food_item` 与既有 `meal-record-save` 求和规则为准
