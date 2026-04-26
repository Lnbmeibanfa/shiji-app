## 1. 数据与配置

- [x] 1.1 新增识别任务表 SQL（与 `file_asset`、`user` 外键及索引：`task_id` 唯一、`user_id+created_at`、`file_id`、`status`）
- [x] 1.2 增加任务 SLA 与保留策略配置项（如 `recognition.task-timeout-seconds=60`、保留天数 7）

## 2. 后端 API 与领域逻辑

- [x] 2.1 实现 `POST` 创建任务：校验 `file_id` 归属与状态，写入 `pending`，异步执行识别
- [x] 2.2 实现 `GET` 轮询：按 `taskId` + `userId` 返回 `status`；成功时 `result` 形状对齐 `DishIngredientVisionResponse`
- [x] 2.3 实现 60s 超时终态：调度或惰性检查将超时的 `pending/processing` 置为失败并写入 `error_code`
- [x] 2.4 编排调用现有视觉识别能力（从 `file_id` 取图或 URL，不得在上传接口内触发）
- [x] 2.5 确认不提供后端 `apply`；错误码与 `ApiResponse` 全局约定一致

## 3. 维护与测试

- [x] 3.1 定时或管理任务：清理终态超过 7 天的任务行
- [x] 3.2 单元/集成测试：创建任务、成功 poll、超时失败、越权 `taskId`、非法 `file_id`
- [x] 3.3 更新 `services/api/README.md`：创建任务与 poll 路径、示例与错误码

## 4. 前端（Flutter）

- [x] 4.1 上传成功后单独调用创建任务；轮询间隔 1s，60s 无终态提示失败并允许重新创建任务
- [x] 4.2 成功结果弹窗确认应用；合并进本地草稿；最终保存时提交 `meal-records`
- [x] 4.3 对齐 `record-meal-ai-flow` delta：异步轮询不单独阻塞保存、可取消轮询
