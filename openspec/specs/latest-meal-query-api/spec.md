## ADDED Requirements

### Requirement: 已认证用户可查询最近已发生的一餐
系统 MUST 提供查询最近一餐的只读 HTTP 接口（实现阶段路径建议为 `GET /api/meal-records/latest`，以代码与路由配置为准），且仅允许已认证用户调用。系统 MUST 仅在当前时刻之前（含当前时刻）的记录范围内查询，即 `recorded_at <= now`，并按 `recorded_at` 降序、`id` 降序选择一条作为“最近一餐”。

#### Scenario: 未登录被拒绝
- **WHEN** 请求未携带有效会话令牌
- **THEN** HTTP 401，响应体符合项目统一未授权 JSON 约定

#### Scenario: 仅查询已发生记录
- **WHEN** 用户存在一条未来时间记录与一条已发生记录
- **THEN** 接口 MUST 返回已发生记录，不得返回未来时间记录

#### Scenario: 同时刻按 id 决定稳定顺序
- **WHEN** 同一用户有两条 `recorded_at` 相同的历史记录
- **THEN** 接口 MUST 返回 `id` 更大的那条记录

### Requirement: 响应必须包含最近一餐核心展示字段
当存在最近一餐时，成功响应 MUST 使用 `ApiResponse` 封装且 `code` MUST 为 0，`data` MUST 至少包含 `mealType`、`recordedAt`、`totalEstimatedCalories` 与 `mood`。其中 `recordedAt` SHALL 沿用项目当前 `LocalDateTime` 输出风格；`mood` MUST 同时提供 `emotionCode` 与 `emotionName`。

#### Scenario: 存在主情绪时返回 code 与 name
- **WHEN** 最近一餐存在可解析的主情绪
- **THEN** `data.mood` MUST 包含非空 `emotionCode` 与 `emotionName`

#### Scenario: 先不返回 mealTypeLabel
- **WHEN** 前端调用最近一餐接口
- **THEN** 响应 MUST NOT 强制包含 `mealTypeLabel`，餐别展示文案由客户端映射

### Requirement: 无记录时返回成功空数据
当用户不存在满足条件（`recorded_at <= now` 且可见、未删除）的记录时，接口 MUST 返回成功响应，`code` MUST 为 0，`data` MUST 为 `null`，且 MUST NOT 将该场景作为业务错误返回。

#### Scenario: 用户暂无任何历史记录
- **WHEN** 用户首次使用系统，尚未保存任何一餐
- **THEN** 响应 `code` 为 0 且 `data` 为 `null`

#### Scenario: 主情绪缺失时 mood 置空
- **WHEN** 最近一餐存在但无可用主情绪
- **THEN** 响应中的 `data.mood` MUST 为 `null`
