# AI 识别过程持久化（meal-ai-recognition-schema）

本规范定义「AI 识别过程」与「最终业务确认结果」的数据分层：`meal_recognition_*` 为过程态，`meal_food_item` 为每餐最终采纳的食物行。

---

## ADDED Requirements

### Requirement: meal_recognition_result 记录一次识别任务的主结果

系统 SHALL 提供 `meal_recognition_result` 实体，用于持久化 **单次** AI 识别任务的主结果， SHALL 至少能关联到所属 `meal_record`，并 SHALL 能表达识别模式、结果来源、命中的菜品（若有）、整体置信度、是否需要用户确认、原始 AI 响应、模型与 prompt 版本、状态与失败原因等字段（具体列名以实现为准）。

`meal_recognition_result` SHALL 表示 **过程态**；最终用户确认后写入或修正的 `meal_food_item` SHALL 为 **业务结果态**，二者 SHALL NOT 混为同一职责。

#### Scenario: 命中菜品时记录主结果

- **WHEN** AI 优先命中某标准菜品并返回置信度与原始响应
- **THEN** 系统 SHALL 能在 `meal_recognition_result` 中持久化命中菜品标识或名称、置信度及原始响应（或引用存储）

#### Scenario: 识别失败可留痕

- **WHEN** 识别任务失败或部分成功
- **THEN** 系统 SHALL 能在 `meal_recognition_result` 中记录状态与失败原因，且 SHALL NOT 用静默丢弃替代持久化（若业务选择记录本次任务）

### Requirement: meal_recognition_item 记录识别出的候选食物项明细

系统 SHALL 提供 `meal_recognition_item` 实体，用于在 **未命中菜品或需要拆解** 时，记录本次识别出的候选 `food_item` 或名称快照、分类快照、置信度、估算重量、单位、来源类型与排序等。

识别明细 SHALL 与 `meal_food_item` 区分：前者为 **识别过程输出**；后者为 **用户确认或业务采纳后** 写入每餐的最终行。

#### Scenario: Fallback 拆解多基础食物

- **WHEN** 未命中 `dish` 且 AI 拆解为多个基础食物候选
- **THEN** 系统 SHALL 能将每条候选写入 `meal_recognition_item`（或等价结构），并 SHALL 能在用户确认后生成对应 `meal_food_item`

### Requirement: 快照分层职责

系统 SHALL 将 **餐级快照**（如菜品名称快照）置于 `meal_record`；将 **食物行快照**（如 `food_name_snapshot`、`category_code_snapshot`）置于 `meal_food_item`；将 **AI 原始过程与候选明细** 置于 `meal_recognition_result` / `meal_recognition_item`。

系统 SHALL NOT 依赖仅扩展 `meal_record` 来承载全部层级的快照而省略 `meal_food_item` 或识别过程表上的字段职责。

#### Scenario: 餐级与行级快照并存

- **WHEN** 一餐既命中菜品名称又确认多行食物
- **THEN** 餐级展示所需快照 SHALL 来自 `meal_record`（及关联 `dish`），行级展示所需快照 SHALL 来自各 `meal_food_item`
