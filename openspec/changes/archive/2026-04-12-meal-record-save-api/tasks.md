## 1. 模块与契约对齐

- [x] 1.1 确认路由前缀与命名（如 `POST /api/meal-records`）与现有 `SecurityConfiguration` 中认证规则一致
- [x] 1.2 定义请求/响应 DTO 与校验注解（`recorded_at`、餐别、食物行、图片 `fileId`、情绪引用等）
- [x] 1.3 新增本模块 `ErrorCode` 枚举项（文件不存在、文件归属不符、情绪标签无效等），并与全局 `ControllerAdvice` 行为一致

## 2. 领域逻辑

- [x] 2.1 实现 `recorded_at` → `record_date` 推导（单一 `ZoneId` 来源：配置或常量 `Asia/Shanghai`）
- [x] 2.2 实现食物行 `estimated_calories`（及可选宏量）求和，写入 `meal_record` 对应 total 字段
- [x] 2.3 情绪：仅写入 `meal_record_emotion_rel`；解析 `emotion_tag` 存在且可用；可选在同一事务内按 `emotion_tag.sort_order`、`id` 回填 `primary_emotion_code`
- [x] 2.4 图片：对每个 `file_id` 查询 `file_asset`，校验存在且 `user_id` 等于当前用户，否则抛业务异常

## 3. 持久化与事务

- [x] 3.1 新增 `MealRecord` / `MealFoodItem` / `MealRecordImage` / `MealRecordEmotionRel` 等 JPA 实体与 Repository（与现有 SQL 表字段对齐）
- [x] 3.2 在单一 `@Transactional` 方法中顺序插入主表与子表；失败整单回滚
- [x] 3.3 `FileAssetRepository` 增加按 `id` 与 `userId` 查询或存在性校验方法

## 4. 接口与测试

- [x] 4.1 实现 Controller，返回 `ApiResponse<?>`，成功 `code=0`
- [x] 4.2 编写集成测试：未登录 401；合法创建 200；错误 `file_id` 拒绝且无落库；`record_date` 与 `recorded_at` 时区推导一致；多行热量求和正确

## 5. 文档与收尾

- [x] 5.1 在实现说明或 README 片段中注明 `recorded_at` 时区语义与 `record_date` 关系（若项目有 API 文档则同步）
- [x] 5.2 自检与 `openspec archive` 前置：对照 `specs/meal-record-save/spec.md` 场景走查
