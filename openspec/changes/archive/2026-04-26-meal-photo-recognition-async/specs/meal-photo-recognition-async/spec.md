# 餐图异步识别任务（meal-photo-recognition-async）

本规范定义：用户在 OSS 上传完成后，通过**独立接口**创建**单图**异步识别任务、轮询结果、**60 秒 SLA 超时**由服务端终态失败，以及 poll 响应与 `DishIngredientVisionResponse` 对齐。本能力 **SHALL NOT** 提供将识别结果写入 `meal_record` 的后端 `apply` 接口。

---

## ADDED Requirements

### Requirement: 创建任务不得耦合文件上传接口

系统 SHALL 提供经认证的 HTTP 接口，使用已上传且归属当前用户的 `file_id` 创建**一条**异步识别任务。系统 SHALL NOT 在文件上传完成接口的默认成功路径中自动触发本识别任务。

#### Scenario: 上传成功后需单独请求才创建任务

- **WHEN** 客户端仅完成文件上传并拿到 `file_id`，且未调用创建任务接口
- **THEN** 系统 SHALL NOT 仅因上传成功而创建或启动识别任务

#### Scenario: 已登录用户用合法 file_id 创建任务

- **WHEN** 已登录用户提交当前用户名下、状态有效的 `file_id`
- **THEN** 系统 SHALL 返回新任务标识（如 `taskId`）及初始 `status`，且 SHALL 开始异步处理

### Requirement: Poll 返回状态与结果契约

系统 SHALL 提供经认证的查询接口，按 `taskId` 返回任务 `status`。当 `status` 表示成功时，响应体 SHALL 包含与现有 `DishIngredientVisionResponse` 一致的 **`requestId` 与 `recognition` 结构**（字段名与嵌套语义一致），SHALL NOT 引入第二套识别结果 JSON 形状。当 `status` 表示失败或超时取消时，SHALL 返回稳定可测的错误码与面向用户的说明字段（与项目 `ApiResponse` 约定一致）。

#### Scenario: 成功态返回 recognition

- **WHEN** 任务已成功完成
- **THEN** 查询响应 SHALL 包含 `requestId` 与 `recognition`（含 `schemaVersion`、`dish`、`ingredients`、`dishRejection`、`modelMeta` 等既有语义）

#### Scenario: 失败态不返回伪造 recognition

- **WHEN** 任务已失败或超时终止
- **THEN** 查询响应 SHALL NOT 将不可信内容伪装为成功 `recognition`

### Requirement: 60 秒 SLA 服务端终止

系统 SHALL 在任务创建后的 **60 秒**内尽力完成识别。若 60 秒届满时任务仍未达到成功或失败终态，系统 SHALL 将任务置为**失败/超时**类终态，SHALL NOT 无限期保持处理中；后续查询 SHALL 返回该终态及超时相关错误信息。用户 MAY 通过**新建任务**（再次调用创建接口）对同一 `file_id` 重新识别。

#### Scenario: 超时后查询为终态失败

- **WHEN** 自任务创建起已超过 60 秒且处理仍未正常结束
- **THEN** 系统 SHALL 将任务标记为超时失败（或等价终态），且后续 poll SHALL 返回该失败信息

### Requirement: 单图 MVP

一条异步识别任务 SHALL 仅绑定**一个** `file_id`。系统 SHALL NOT 在本 MVP 中要求或支持同一任务内多图列表。

#### Scenario: 拒绝多图扩展字段

- **WHEN** 客户端在创建任务请求中携带多个 `file_id` 或非约定结构
- **THEN** 系统 SHALL 返回请求无效错误（具体 HTTP/业务码与项目一致）

### Requirement: 任务数据保留约 7 天

系统 SHOULD 在任务到达终态后，将任务记录保留至多约 **7 天**（用于排障与对账），之后 MAY 删除或归档。该保留策略 SHALL NOT 要求客户端在 7 天后仍能依赖 poll 取回结果作为唯一真源（最终业务数据以用户保存餐次请求为准）。

#### Scenario: 过期任务不可再作为可信来源

- **WHEN** 任务已超过保留期限被清理
- **THEN** 查询 MAY 返回不存在或已过期语义，客户端 SHALL 通过重新识别或依赖本地草稿恢复
