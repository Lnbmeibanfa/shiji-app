## Why

需要在服务端提供「单张菜品照片 → 受控目录命中」能力：优先命中 `dish`（经 `dish_alias` 别名映射），否则在全局 `food_item` 食材词表内拆解候选，以便与现有点餐/记餐数据模型对齐。MVP 采用整表词表进 prompt + 通义多模态，后续再演进到向量检索以控制成本与上下文长度。

## What Changes

- 新增**同步** HTTP API：接受单图，经**保守压缩**后调用 DashScope 多模态模型；模型**仅允许输出 JSON**（由 prompt 约束 + 服务端解析校验）。
- 服务端将模型返回的**别名字符串**映射为 `dish_id`（BIGINT）；食材同理映射到 `food_item` id。
- 统一 **τ = 0.7**：菜品唯一候选且 `confidence ≥ 0.7` 时 `dish` 合法；否则 `dish` 为空并进入食材模式；每个食材须带 `confidence`，且**最多 10 条**。
- 响应体为结构化 JSON，包含 `request_id`、追踪字段；**拒识原因**对调用方可见（`dish_rejection` 等）。
- 对**非食物图**、**完全无法识别**等边界，返回**明确业务/HTTP 错误码**（非静默空 200）。
- 新增版本化 prompt 资源文件（符合仓库内 AI 能力规范）。

## Capabilities

### New Capabilities

- `dish-ingredient-vision`：单图、全局词表、别名映射、置信度阈值、结构化响应与错误码、同步调用约定。

### Modified Capabilities

- （无）本变更不修改 `openspec/specs/` 下已有条文的语义；实现时可复用 `meal-dish-catalog-schema` / `meal-ai-recognition-schema` 的表结构，但不在此变更中做 delta 需求改写。

## Impact

- **服务**：`services/api` 新增或扩展 AI 模块（多模态客户端、压缩、映射、错误码）。
- **配置**：DashScope 视觉模型名、超时、阈值（默认 0.7，可配置）、压缩参数。
- **数据**：读取全局 `dish`、`dish_alias`、`food_item`（或等价命名）；id 对外 JSON 以十进制字符串表示 BIGINT，避免 JS 精度问题。
- **客户端**：需按新契约解析响应与错误码（可由后续 Flutter 契约变更承接）。
