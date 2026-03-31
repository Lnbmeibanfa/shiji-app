## Why

食迹主链路需要「拍照 → 持久化 → 展示」。第一版采用**后端代理上传**：客户端将图片提交到 Spring Boot，由服务端调用阿里云 OSS **PutObject（简单上传）** 写入对象存储，再返回可访问 URL 与 `file_asset` 主键，打通闭环并为二期 STS 直传预留同一套表结构与领域模型。

## What Changes

- 新增 **已登录** 可调用的 HTTP 接口：`multipart/form-data` 上传图片，服务端写入 OSS 并落库 `file_asset`。
- 引入 **阿里云 OSS Java SDK** 与可配置项（endpoint、region、bucket、credentials，凭证来自环境变量或配置，不提交仓库）。
- 新增 `modules/file`（或等价包名）下的 Controller / Service / Repository / Entity，与 `create_file_asset.sql` 对齐。
- 定义模块级错误码（文件过大、类型不允许、OSS 失败等），经全局 `ApiResponse` 返回。
- **非**本期范围：STS 直传、POST Policy、分片上传、meal_record 自动创建（可后续单独变更）。

## Capabilities

### New Capabilities

- `file-upload`：后端代理 OSS 上传、持久化文件资产、对外 JSON 契约与校验规则。

### Modified Capabilities

- （无）`api-http-contract` 行为不变，仍使用 `ApiResponse`；仅新增调用方与错误码枚举。

## Impact

- **代码**：`services/api`（新模块、配置、依赖、Security 无需改白名单——上传走默认已认证）。
- **配置**：部署环境需提供 OSS 连接与密钥；本地可用占位或 dev profile。
- **数据库**：依赖已执行的 `file_asset` 表；`object_key_hash` 由应用按约定写入。
