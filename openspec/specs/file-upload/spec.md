# 文件上传（file-upload）

本规范定义后端代理上传至阿里云 OSS 的第一版行为：已认证用户通过 multipart 上传、OSS PutObject、file_asset 落库与校验规则。

---

## 规范要求

### Requirement: 已登录用户可上传图片至 OSS 并落库

系统 MUST 提供 `POST /api/files/upload` 接口，仅接受**已认证**用户调用。请求 MUST 使用 `multipart/form-data`，且 MUST 包含名为 `file` 的文件部件。服务端 MUST 使用阿里云 OSS **PutObject** 将文件写入配置的 Bucket，并 MUST 在 `file_asset` 表中插入一条记录。成功响应 MUST 使用 `ApiResponse` 封装，且 `data` MUST 包含至少：`fileId`（即 `file_asset.id`）、`url`（用于前端展示）、`objectKey`、`bucket`。

#### Scenario: 上传成功

- **WHEN** 已登录用户提交符合类型与大小限制的 `file`
- **THEN** HTTP 200，且 `code` 为 0，`data` 中包含 `fileId` 与可访问的 `url`（在当前部署的访问模型下），且数据库中对应 `file_asset` 行的 `status` 为 `uploaded`、`upload_source` 为 `backend_proxy`

#### Scenario: 未登录被拒绝

- **WHEN** 请求未携带有效会话令牌
- **THEN** HTTP 401，且响应体符合项目统一未授权 JSON 约定

### Requirement: 上传校验与拒绝原因可展示

系统 MUST 拒绝超过配置最大体积或 MIME 不在白名单内的文件，且 MUST 通过 `ApiResponse` 返回非 0 `code` 与可读 `message`。OSS 调用失败时 MUST 返回业务错误码，且 MUST NOT 将内部堆栈暴露给客户端。

#### Scenario: 文件过大

- **WHEN** 上传文件超过配置的最大字节数
- **THEN** 请求失败，`message` 说明体积超限

#### Scenario: 类型不允许

- **WHEN** 文件 Content-Type 不在允许列表中
- **THEN** 请求失败，`message` 说明类型不允许

### Requirement: file_asset 字段与哈希约定

插入 `file_asset` 时，系统 MUST 写入 `user_id`（当前登录用户）、`storage_provider`（如 `aliyun_oss`）、`bucket`、`object_key`、`object_key_hash`（对 `bucket + "\n" + object_key` 的 UTF-8 字节做 SHA-256 十六进制小写字符串）、`url`、`file_name`、`content_type`、`file_size`、`etag`（若 OSS 响应提供）、`upload_source`、`status=uploaded`、`biz_type`（默认 `meal_photo` 或可配置）、`uploaded_at`、`created_at`、`updated_at`。`object_key_hash` MUST 与表上唯一约束 `uk_file_object_hash` 一致，禁止重复插入同一逻辑对象。

#### Scenario: 唯一性

- **WHEN** 同一 `bucket` 与 `object_key` 已存在
- **THEN** 插入 MUST 失败或业务层 MUST 在生成 key 时保证 UUID 唯一性使该情况不出现于正常路径

### Requirement: 配置与密钥不得硬编码

OSS endpoint、region、bucket、访问凭证 MUST 来自 Spring 配置或环境变量；仓库内 MUST NOT 提交真实 AccessKey Secret。生产部署 MUST 使用 RAM 子账号并遵循最小权限（如仅目标 bucket 与对象前缀）。

#### Scenario: 本地开发

- **WHEN** 开发者未配置 OSS
- **THEN** 应用 MAY 通过 profile 禁用上传或提供明确启动失败说明（实现任选其一并在文档中固定）
