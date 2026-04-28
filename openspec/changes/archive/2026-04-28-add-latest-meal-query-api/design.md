## Context

当前 `meal` 模块已有保存接口（`POST /api/meal-records`）和完整落库字段，但缺少面向展示层的「最近一餐」读取能力。记录饮食页与首页都存在“最近记录/最近一餐”展示位，若没有统一查询接口，前端会出现查询口径不一致（是否包含未来时间、无数据如何返回、心情字段是否完整）的问题。

本次变更仅新增后端读取能力，不引入新表、不改历史写入逻辑，复用现有 `meal_record` 与 `emotion_tag` 数据。

## Goals / Non-Goals

**Goals:**
- 提供一个已认证可调用的最近一餐查询 API。
- 固化“最近”的定义：仅已发生记录（`recorded_at <= now`），按 `recorded_at desc, id desc` 选一条。
- 返回前端展示必需字段：餐别、记录时间、热量、心情（`emotionCode` + `emotionName`）。
- 固化空态语义：无记录时 `code=0, data=null`。
- 保持与现有序列化风格一致：`recordedAt` 继续使用当前项目 `LocalDateTime` 输出风格。

**Non-Goals:**
- 不改动 `POST /api/meal-records` 保存流程。
- 不返回 `mealTypeLabel`（前端继续映射）。
- 不在本变更中改动 `record-meal-ui` 页面规范（是否启用真实接口由后续客户端变更决定）。

## Decisions

### Decision 1: API 形态使用单资源读取端点
- 方案：新增 `GET /api/meal-records/latest`，不要求 `mealType` 参数。
- 理由：当前需求是全局“最近一餐”，固定查询语义可减少客户端分支与参数校验复杂度。
- 备选：
  - `GET /api/meal-records?sort=latest&size=1`：通用但过度抽象，首版复杂度更高。
  - 强制 `mealType`：与当前业务目标不符。

### Decision 2: “最近”按已发生时间计算
- 方案：查询条件包含 `recorded_at <= now`，并限定可见/未删除记录；结果按 `recorded_at desc, id desc` 取首条。
- 理由：避免未来时间记录（预录入）误占“最近一餐”，符合用户认知。
- 备选：
  - 直接取最大 `recorded_at`：会把未来记录当作最近，语义偏差。

### Decision 3: 心情字段采用复合对象并允许 null
- 方案：返回 `mood` 对象，包含 `emotionCode` 与 `emotionName`；当缺失主情绪时 `mood = null`。
- 理由：前端展示与埋点都依赖 code/name，且 null 语义最清晰，避免“空字符串对象”歧义。
- 备选：
  - 仅返回 `emotionCode`：前端需二次字典映射，增加耦合。
  - 固定返回空对象：会增加前端额外判空逻辑。

### Decision 4: 空结果成功返回
- 方案：无可用记录时返回 `ApiResponse(code=0, data=null)`。
- 理由：最近一餐不存在属于正常业务空态，不应被建模为错误流程。
- 备选：
  - 返回业务错误码：会让前端在“空态”与“异常”处理上混淆。

## Risks / Trade-offs

- [风险] `primary_emotion_code` 与 `emotion_tag` 名称映射缺失，导致 name 无法返回  
  → Mitigation：当映射不到标签时返回 `mood=null`，保持接口稳定；后续可补数据治理任务。

- [风险] `now` 的时区与写入口径不一致，边界记录可能出现偏差  
  → Mitigation：沿用服务端统一时区/时钟源，与现有 `recorded_at` 判定逻辑保持一致，并在集成测试覆盖边界时间。

- [权衡] 首版不返回 `mealTypeLabel`，前端仍需本地映射  
  → Mitigation：维持当前前后端职责边界，若后续多端统一文案需求增强，再通过独立变更扩展字段。
