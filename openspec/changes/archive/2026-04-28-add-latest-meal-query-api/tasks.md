## 1. API 契约与模块接口

- [x] 1.1 在 `meal` 模块新增最近一餐查询的 Controller 路由（建议 `GET /api/meal-records/latest`），接入现有认证主体并返回 `ApiResponse`
- [x] 1.2 新增响应 DTO（含 `mealType`、`recordedAt`、`totalEstimatedCalories`、`mood.emotionCode`、`mood.emotionName`）并明确 `mood` 可空
- [x] 1.3 在 Service 接口与实现中新增 `getLatestMeal` 方法，固定无 `mealType` 参数的查询契约

## 2. 数据查询与组装逻辑

- [x] 2.1 在 Repository 增加最近一餐查询方法：限定 `user_id`、`deleted_at is null`、`visibility_status=1`、`recorded_at <= now`，按 `recorded_at desc, id desc` 取 1 条
- [x] 2.2 在 Service 层实现空结果分支：无记录时返回 `data=null`（`code=0`）
- [x] 2.3 实现心情组装逻辑：存在可用主情绪时返回 `emotionCode + emotionName`，缺失时返回 `mood=null`
- [x] 2.4 保持时间序列化风格与项目现有 `LocalDateTime` 输出一致，不新增 `mealTypeLabel`

## 3. 测试与回归验证

- [x] 3.1 增加集成测试：未登录返回 401
- [x] 3.2 增加集成测试：存在未来记录与历史记录时，仅返回 `recorded_at <= now` 的最近记录
- [x] 3.3 增加集成测试：无记录返回成功且 `data=null`
- [x] 3.4 增加集成测试：主情绪存在返回 `mood` 对象，主情绪缺失返回 `mood=null`
