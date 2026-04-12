# Flutter HTTP 与 API 契约（flutter-http-contract）— 变更增量

本文件为相对 `openspec/specs/flutter-http-contract/spec.md` 的 **ADDED** 增量：在保持既有 JSON 与单一出口要求不变的前提下，增加 multipart 上传约定。

---

## ADDED Requirements

### Requirement: 经 ApiClient 的 multipart 文件上传与 ApiResponse 解析

对需要上传文件的接口（如 `POST /api/files/upload`），客户端 MUST 仍通过 `ApiClient` 发起请求。`ApiClient` MUST 支持使用 `multipart/form-data` 提交至少一个文件字段，且成功响应 MUST 使用与 JSON 接口相同的 `{ "code", "message", "data" }` 封装；当 `code == 0` 时，业务层 MUST 能将 `data` 解析为对应 DTO（如含 `fileId`、`url`）。`ApiClient` MUST NOT 强制全局 `Content-Type: application/json` 覆盖 multipart 请求的边界类型。

#### Scenario: multipart 成功

- **WHEN** 客户端通过 `ApiClient` 上传文件且服务端返回 HTTP 200 且 JSON 中 `code` 为 `0`
- **THEN** 调用方获得解析后的 `data` 对象，且请求在传输层使用 multipart 编码

#### Scenario: multipart 业务失败

- **WHEN** 响应 JSON 中 `code` 不为 `0`
- **THEN** 行为与既有「业务失败」要求一致：抛出或返回可识别业务错误

#### Scenario: 禁止旁路 Dio

- **WHEN** `features/` 下模块需要上传文件
- **THEN** 请求 MUST 经由 `ApiClient` 的 multipart 能力，而非新建独立 `Dio` 仅用于上传
