# 标准食物分页搜索 API（food-item-search）

本规范定义按名称模糊查询 `food_item` 并分页返回、用于「添加食物」列表的 HTTP 行为。

---

## ADDED Requirements

### Requirement: 已认证用户可分页搜索可用标准食物

系统 MUST 提供只读 HTTP 接口，使已登录用户能够按可选关键词对标准食物做**模糊**名称检索，并**分页**返回结果。结果 MUST 仅包含 `edible_status = 1`（可用）的食物。未认证请求 MUST 被拒绝。

#### Scenario: 未登录被拒绝

- **WHEN** 请求未携带有效会话令牌
- **THEN** HTTP 401，响应符合项目统一未授权约定

#### Scenario: 无关键词时返回第一页

- **WHEN** 请求未携带 `q` 或 `q` 为空（或仅空白）
- **THEN** HTTP 200，返回按实现约定排序的第一页可用食物（例如按 `id` 或 `food_name` 升序），**不得**因未传 `q` 而失败

### Requirement: 名称模糊匹配与分页参数

查询参数 MUST 支持：`q`（可选，用于匹配 `food_item.food_name`，实现 MAY 同时匹配 `food_alias`）；`page`（非负整数，**0-based**）；`size`（正整数，默认 20，且 MUST 有上限如 50）。响应 MUST 标明当前页是否还有下一页（例如 `hasNext` 或等价字段）。

#### Scenario: 模糊匹配子串

- **WHEN** `q` 为某非空字符串
- **THEN** 返回的食物名称（或别名）MUST 满足数据库侧模糊匹配语义（如 `LIKE %q%`），且分页正确

#### Scenario: 触底加载下一页

- **WHEN** 同一 `q` 下第一次请求 `page=0`，第二次请求 `page=1`，`size` 相同
- **THEN** 第二次返回的集合与第一次**不重复**（除非实现明确允许重复），且当无更多数据时 `hasNext` 为 false

### Requirement: 列表项包含每 100g 热量展示字段

每个 `food_item` 条目 MUST 尽量包含用于展示「X kcal/100g」的数值字段。热量 MUST 来自 `food_nutrition` 中与 `nutrient_basis = per_100g` 对应的记录；若不存在该营养行，则该字段 MAY 为 `null`，且 MUST NOT 导致整请求失败。

#### Scenario: 有 per_100g 营养数据

- **WHEN** 某 `food_item` 在 `food_nutrition` 中存在 `per_100g` 且 `calories` 有值
- **THEN** 响应中该条目的热量字段（如 `caloriesPer100g`）MUST 与该值一致

### Requirement: 业务错误与统一响应

成功时 MUST 使用 `ApiResponse` 封装，`code` MUST 为 0。参数非法（如 `size` 超过上限、`page` 负数）时 MUST 返回非 0 `code` 与可读 `message`，**MUST NOT** 暴露内部堆栈。

#### Scenario: page 为负数

- **WHEN** `page` 小于 0
- **THEN** 请求失败，`code` 非 0
