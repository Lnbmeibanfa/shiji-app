## Context

- 栈：Spring Boot 4.x、Java 21、JPA、现有 `ApiResponse` / `SecurityConfiguration`（除白名单外均需登录）。
- 表：`file_asset`（见 `services/api/src/main/resources/sql/create_file_asset.sql`），`object_key_hash` 为 **64 位十六进制**，与 `bucket + object_key` 的哈希约定一致。
- 阿里云：单对象 **简单上传** 使用 **PutObject**（单请求，适合餐图体积）；见[简单上传](https://help.aliyun.com/zh/oss/simple-upload) 与 OSS Java SDK。

## Goals / Non-Goals

**Goals:**

- 已登录用户上传图片 → OSS PutObject → 插入 `file_asset`（`upload_source=backend_proxy`，`status=uploaded`）→ 返回 `fileId`、`url`、`objectKey`、`bucket` 等前端展示所需字段。
- 校验：Content-Type 白名单（如 `image/jpeg`、`image/png`、`image/webp`）、最大体积（可配置，如 10MB）。
- `object_key` 命名：`users/{userId}/meals/{yyyyMMdd}/{uuid}.{ext}` 或等价可扩展前缀，便于生命周期与二期直传前缀策略一致。
- `object_key_hash`：**SHA-256（十六进制小写）**，输入字符串为 UTF-8 编码的 `bucket + "\n" + object_key`（与实现单元测试锁死，避免与注释「bucket + object_key」歧义）。

**Non-Goals:**

- 客户端直传、STS、PostObject、上传回调、分片、断点续传。
- 自动生成缩略图、病毒扫描、AI 分析。
- 在本变更中创建 `meal_record`（仅文件上传）。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| OSS 访问方式 | 服务端 RAM 用户 AccessKey + Secret（或 STS 仅服务端使用） | MVP 最快；密钥仅服务端 |
| SDK | `com.aliyun.oss:aliyun-sdk-oss`（版本与 Spring Boot BOM 兼容） | 官方维护，PutObject 文档齐全 |
| URL 返回 | 基于配置的 **公网或绑定域名** 拼接 `https://{bucket}.{endpointHost}/{objectKey}` 或使用 SDK 的 URL 构造方式 | 需与 bucket 读写权限一致；私网读需后续改为签名 URL |
| 事务边界 | 先 OSS 成功再 DB；若 DB 失败则记录日志并 **可补偿删除 OSS 对象**（首期可简化为日志 + 定时清理） | 避免「只落库未上传」；孤儿文件后续用任务扫 |
| Controller 形态 | `POST /api/files/upload`，`consumes = MULTIPART_FORM_DATA`，字段名 `file` | 与 Flutter `MultipartFile` 常见约定一致 |

**备选（未采用）：** 先插 DB `pending` 再上传——多一次状态迁移，MVP 不必。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 服务端带宽与 CPU 成为瓶颈 | 二期切直传；本期限流与大小限制 |
| 密钥泄露 | 环境变量、RAM 最小权限、仅 Put/List 所需前缀 |
| URL 长期有效 vs 私桶 | 产品定 bucket ACL；文档中注明若私桶需签名 URL |
| DB 与 OSS 不一致 | 首期日志；后续 `complete`/清理任务 |

## Migration Plan

- 新增依赖与配置后部署；无旧数据迁移。
- 回滚：移除路由与依赖、关闭配置；已上传 OSS 对象保留或脚本清理。

## Open Questions

- 生产环境 Bucket 为 **公共读** 还是 **私有 + CDN 签名**（影响返回 `url` 字段形态）。
- 是否需要在首版写入 `content_hash`（文件字节 SHA-256）用于去重（可选）。
