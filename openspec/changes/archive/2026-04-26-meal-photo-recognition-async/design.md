## Context

- 已有 OSS 上传与 `file_asset`；已有同步视觉识别实现（`DishIngredientVisionResponse` / `recognition` 结构）。
- 产品要求：**上传与识别解耦**；前端在拿到 `file_id` 后**显式调用**识别接口；**轮询**获取结果；**60s** 内无终态则由**服务端终止任务**并返回失败语义，前端提示并可**重新识别**；**无后端 apply**；**单图**；识别结果在最终保存 `meal_record` 前以**前端草稿**为主；任务数据建议保留 **7 天** 后清理。

## Goals / Non-Goals

**Goals:**

- 提供创建异步任务、查询任务状态的 HTTP API（需登录，与项目安全惯例一致）。
- 成功态下 poll 返回的 `result` 与 `DishIngredientVisionResponse`（`requestId` + `recognition`）字段语义对齐，避免第二套 JSON。
- 任务创建后 **60 秒** SLA：未达终态则由服务端标记失败/超时并停止继续推理（实现可选用定时扫描、`@Scheduled` 或惰性检查）。
- 支持用户对同一 `file_id` 在失败后**新建任务**重试（不做复杂全局去重，必要时在实现层限流）。

**Non-Goals:**

- 后端 `apply` 或写入 `meal_record` / `meal_recognition_*`（留待用户点击保存餐次时一次性提交）。
- 一单任务多图、批量识别队列产品化。
- WebSocket / SSE 推送（MVP 仅用轮询）。

## Decisions

1. **任务存储**  
   - 采用关系表（如 `ai_recognition_task` 或与现有 `create_recognition_task.sql` 对齐的表名），字段至少包含：`task_id`（对外 UUID）、`user_id`、`file_id`、`status`、`error_code`、`error_message`、`result_json`（成功时的 recognition 快照）、时间戳与 `deadline_at` / `started_at` / `finished_at`。  
   - **备选**：纯 Redis — 未选，因需 7 天可查与审计，MySQL 更直观。

2. **识别编排**  
   - Worker/异步线程：创建任务后置 `processing`，从 OSS/`file_asset` 拉取或生成模型输入，调用现有视觉识别服务；写回 `result_json` 与终态。  
   - **超时**：创建时记录 `created_at`，轮询或调度器发现 `now - created_at >= 60s` 且仍为 `pending/processing` → 置 `failed`，`error_code` 如 `RECOGNITION_TIMEOUT`。

3. **API 形态**  
   - `POST /api/.../recognitions` body: `{ "fileId": <long> }` → `{ "taskId", "status" }`。  
   - `GET /api/.../recognitions/{taskId}` → `status` + `result`（结构同 `DishIngredientVisionResponse`）+ `errorCode`/`errorMessage`（失败时）。  
   - **不在** `POST /api/files/...` 上传链路内触发识别。

4. **轮询与前端**  
   - 建议间隔 **1s**；前端 **60s** 无终态可停止轮询并提示（与后端 SLA 对齐）；后端仍可能稍晚将任务标超时，以 poll 为准。

5. **数据保留**  
   - 定时任务删除 `finished_at` 超过 **7 天** 的行，或标记归档；实现细节在 tasks 中落地。

## Risks / Trade-offs

- **[轮询压力]** → 任务量小可接受；后续可加长间隔或引入推送。  
- **[超时与真实推理竞态]** → 超时后忽略晚到写回或乐观锁更新，避免覆盖终态。  
- **[前端草稿丢失]** → 产品接受；依赖最终保存提交。

## Migration Plan

- 新增表与 API，**不破坏**现有同步识别接口。  
- 部署顺序：先表与后端，再 Flutter 切换调用链。  
- 回滚：关闭新路由或 feature flag，前端回退为不上传后识别。

## Open Questions

- 超时错误码是否与现有 `AiErrorCode` 共用命名空间，还是任务域独立前缀（如 `32xxx`）。  
- `file_id` 校验是否强制 `biz_type=meal_photo`（与现网一致即可）。
