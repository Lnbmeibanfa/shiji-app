## Why

记录饮食页面已存在「最近一餐」展示位，但当前规范仍是占位，缺少可复用的后端读取接口。为了让客户端稳定展示最近已发生的一餐（含餐别、记录时间、能量、心情），需要新增统一查询契约，避免各端自行拼装查询规则导致口径不一致。

## What Changes

- 新增「查询最近一餐」HTTP API（已认证用户可调用，返回统一 `ApiResponse`）。
- 固化查询语义：仅查询已发生记录（`recorded_at <= now`），按时间倒序选取最接近当前时间的一餐。
- 固化返回字段：`mealType`、`recordedAt`、`totalEstimatedCalories`、`mood`（同时返回 `emotionCode` 与 `emotionName`）。
- 固化空结果语义：无记录时返回 `code=0, data=null`，不抛业务错误。
- 保持当前展示分工：不返回 `mealTypeLabel`，由前端继续自行映射。

## Capabilities

### New Capabilities

- `latest-meal-query-api`：定义最近一餐查询接口的认证、过滤、排序、返回字段与空结果语义。

### Modified Capabilities

- （无）本次先新增后端读取能力，不修改既有 `record-meal-ui` 的页面占位条款；客户端何时切换真实数据由后续变更承接。

## Impact

- **后端 API**：在 `meal` 模块新增读取端点及对应 DTO/Service/Repository 查询方法。
- **数据访问**：基于现有 `meal_record` 与 `emotion_tag` 数据，不要求新增表结构。
- **客户端**：后续可直接消费该接口展示最近一餐与心情文案；当前无需改动餐别文案映射逻辑。
