## 1. 配置与依赖

- [x] 1.1 在 `services/api` 增加 DashScope 多模态模型名、超时、默认 τ=0.7、图像最大边长/质量等配置项（与 `application.yml` / 本地示例对齐）
- [x] 1.2 确认 `pom.xml` 或 HTTP 客户端足以调用多模态 API（与现有 `DashScopeTextClient` 模式一致或扩展）

## 2. Prompt 与模板

- [x] 2.1 新增 `src/main/resources/prompts/...` 版本化 prompt：仅输出 JSON、`outcome` 边界（NOT_FOOD / UNRECOGNIZABLE）、词表占位说明、字段 schema 说明
- [x] 2.2 实现词表注入：加载全局 `dish`、`dish_alias`、`food_item`，拼接进 prompt（当前全量；注意性能与缓存）

## 3. 图像预处理

- [x] 3.1 实现保守压缩/缩放管线：可测的最大边长、格式与质量策略，保证识别质量优先
- [x] 3.2（可选）记录原图 hash、处理后尺寸进 `model_meta` 或日志

## 4. 多模态调用与解析

- [x] 4.1 实现 `DashScope*Vision*`（或扩展现客户端）：单图请求、同步响应
- [x] 4.2 严格 JSON 提取与校验；失败映射 `MODEL_OUTPUT_INVALID`
- [x] 4.3 将模型 `outcome` 映射为 HTTP/业务错误：`NOT_FOOD_IMAGE`、`UNRECOGNIZABLE_IMAGE` 等

## 5. 业务校验与映射

- [x] 5.1 别名字符串 → `dish_id` / `alias_id`（多命中同 dish 取最小 `alias_id`）
- [x] 5.2 应用 τ=0.7 规则：`dish` 唯一性、`AMBIGUOUS` / `LOW_CONFIDENCE` / `NO_CATALOG_MATCH`
- [x] 5.3 食材：映射 `food_item`、过滤低置信度、排序截断 10 条
- [x] 5.4 组装 `recognition` 响应：`schema_version`、`request_id`、BIGINT 字符串化

## 6. API 与错误处理

- [x] 6.1 暴露同步 REST 端点（路径与鉴权与项目惯例一致）
- [x] 6.2 与全局异常处理对齐：`UPSTREAM_AI_ERROR`、`MODEL_OUTPUT_INVALID`、边界错误码及用户可读提示文案
- [x] 6.3（可选）将识别过程写入 `meal_recognition_*` 与现有记餐流衔接——本次按 API MVP 实现，记餐流程持久化留作后续集成任务

## 7. 测试

- [x] 7.1 单元测试：映射、阈值、截断、歧义、JSON 解析失败
- [x] 7.2 集成测试（Mock 上游或 test 密钥）：成功、dish-only、ingredients fallback、NOT_FOOD
