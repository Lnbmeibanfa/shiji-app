## 1. 依赖与配置

- [x] 1.1 在 `services/api/pom.xml` 增加阿里云 OSS Java SDK（`aliyun-sdk-oss` 或与官方 BOM 对齐的版本）
- [x] 1.2 在 `application.yml`（或 `application-dev.yml`）增加 OSS 配置项：`endpoint`、`region`、`bucket`、`access-key-id`、`access-key-secret`（占位符），并用环境变量覆盖敏感项；在 README 或 `docs/backend-local-development.md` 增加说明

## 2. 领域与持久化

- [x] 2.1 新增 `FileAssetEntity` 与 `file_asset` 表字段一一映射（含 `object_key_hash` 由代码写入，不映射为数据库生成列）
- [x] 2.2 新增 `FileAssetRepository`
- [x] 2.3 实现 `ObjectKeyHash` 工具：`SHA-256` 十六进制（小写），输入 `bucket + "\n" + object_key` UTF-8 字节

## 3. OSS 封装

- [x] 3.1 新增 `OssProperties`（`@ConfigurationProperties`）绑定 OSS 配置
- [x] 3.2 新增 `OssClientFactory` 或 `@Bean` 构建单例 `OSS` 客户端（按需）
- [x] 3.3 实现 `FileStorageService`（或 `AliyunOssStorageService`）：根据 userId 生成 `objectKey`，`PutObject` 设置 `Content-Type`，读取返回 `ETag`，拼接 `url`

## 4. HTTP API

- [x] 4.1 新增 `FileUploadController`：`POST /api/files/upload`，`@RequestParam("file") MultipartFile file`，从 `SecurityContext` 取当前用户 id
- [x] 4.2 校验：文件非空、MIME 白名单、`file.getSize()` 上限；失败抛业务异常映射为 `ApiResponse`
- [x] 4.3 定义 `FileErrorCode`（或 `StorageErrorCode`）实现 `ErrorCode`：过大、类型不允许、OSS 失败等
- [x] 4.4 响应 DTO：`fileId`、`url`、`objectKey`、`bucket`、`contentType`、`size`（按需）

## 5. 验证

- [x] 5.1 本地配置真实或 mock OSS（若暂无云资源，可集成测试 Mock OSS 客户端或 Testcontainers，至少保证 `ObjectKeyHash` 与 Controller 单测）
- [x] 5.2 `./mvnw -pl services/api test` 通过；手动用 curl/Postman 带 token 调上传一次（若已配置 OSS）
