# 食迹 API：阿里云 OSS 后端代理上传技术方案（v1）

## 文档信息

| 字段 | 内容 |
|------|------|
| 文档版本 | v1.0 |
| 关联变更 | `openspec/changes/api-oss-proxy-upload-v1` |
| 服务模块 | `services/api` → `com.shiji.api.modules.file` |
| 状态 | 已实现（MVP） |
| 最后更新 | 2026-03-31 |

### 修订记录

| 版本 | 日期 | 摘要 |
|------|------|------|
| v1.0 | 2026-03-31 | 与当前代码与设计文档对齐的定稿 |

---

## 1. 背景

### 1.1 业务背景

食迹主链路需要「拍照 → 持久化 → 展示」。第一版需尽快打通闭环：移动端上传餐图，服务端持久化存储并返回可展示 URL 与业务侧可用的 `fileId`。

### 1.2 技术现状

在引入本方案前，应用侧无统一文件上传与资产表；对象存储未接入。

### 1.3 待解决问题

1. 提供**已登录用户**可用的、与现有 `ApiResponse` / 安全模型一致的上传接口。
2. 将文件写入阿里云 OSS，并落库 `file_asset`，保证 `object_key` 与哈希约定可支撑二期 **STS 直传**（同一表结构、前缀可延续）。
3. 对体积与 MIME 做强校验，失败原因可对用户展示；OSS 异常不泄露内部堆栈。

---

## 2. 目标与非目标

### 2.1 目标

| 编号 | 目标 | 验收标准 |
|------|------|----------|
| G1 | 已登录用户可通过 HTTP 上传图片至 OSS | `POST /api/files/upload` 成功时 OSS 存在对象且 `file_asset` 有对应行 |
| G2 | 响应包含前端展示所需字段 | `data` 含 `fileId`、`url`、`objectKey`、`bucket`（及 `contentType`、`size`） |
| G3 | 校验可配置、可预期 | 超限与非法类型返回业务码与可读 `message` |
| G4 | 密钥与 endpoint 不入库 | 生产凭证经环境变量注入；仓库无真实 Secret |

### 2.2 非目标（本期明确不做）

- 客户端直传、STS、PostObject、OSS 回调、分片上传、断点续传。
- 自动生成缩略图、病毒扫描、AI 分析。
- 上传接口自动创建 `meal_record`（仅文件域闭环）。

---

## 3. 名词与约定

| 术语 | 含义 |
|------|------|
| 后端代理上传 | 客户端将文件 POST 至 API，服务端读流后调用 OSS **PutObject** |
| `object_key` | OSS 对象路径；本期格式见 §6 |
| `object_key_hash` | 对 UTF-8 字节串 `bucket + "\n" + object_key` 做 **SHA-256**，输出 **64 位十六进制小写**（与唯一索引 `uk_file_object_hash` 一致） |
| `upload_source` | 本期固定写入 `backend_proxy`；二期直传为 `client_direct` |

---

## 4. 需求与约束

### 4.1 功能需求

| 编号 | 需求 | 说明 |
|------|------|------|
| F1 | 接口形态 | `POST /api/files/upload`，`Content-Type: multipart/form-data`，文件字段名 **`file`** |
| F2 | 认证 | 走默认 Spring Security；**未登录 401** |
| F3 | OSS 写入 | 使用官方 Java SDK `PutObject`（简单上传） |
| F4 | 落库 | 插入 `file_asset`，`status=uploaded`，`biz_type=meal_photo` |

### 4.2 非功能与安全

| 维度 | 要求 |
|------|------|
| 配置 | `shiji.oss.*`；`enabled=false` 时无 OSS Bean，接口返回「对象存储未配置」 |
| 上传限制 | Spring `multipart` 与业务侧 `max-file-size-bytes` 对齐（当前默认 10MB） |
| 类型白名单 | `image/jpeg`、`image/png`、`image/webp`（含 `Content-Type` 带参数的规范化） |
| RAM | 生产建议使用子账号 **最小权限**（目标 bucket + 必要前缀） |

### 4.3 硬约束

- 与现有 `file_asset` DDL（`services/api/src/main/resources/sql/create_file_asset.sql`）一致。
- 错误经模块级 `FileExceptionHandler` 转为 `ApiResponse`，业务码区间 **20001–20005**（文件模块）。

---

## 5. 方案总览

**推荐结论（一句话）**：第一版采用 **服务端 RAM 凭证 + PutObject 后端代理上传**，先 OSS 成功再写库；公网 URL 采用 **虚拟主机风格** 拼接，与 bucket 读权限模型一致。

```text
[客户端] --multipart--> [API Gateway / Spring]
       --> [FileUploadController]
       --> [FileStorageServiceImpl] --PutObject--> [阿里云 OSS]
       --> [file_asset INSERT]
       <-- ApiResponse(data: fileId, url, ...)
```

---

## 6. 方案详述

### 6.1 HTTP 接口

| 属性 | 值 |
|------|-----|
| Method / Path | `POST /api/files/upload` |
| 消费类型 | `multipart/form-data` |
| 参数 | `file`（`MultipartFile`） |
| 认证 | `@AuthenticationPrincipal AuthPrincipal` → `userId` |

成功时 `ApiResponse` 的 `data` 类型为 `FileUploadResponse`：`fileId`、`url`、`objectKey`、`bucket`、`contentType`、`size`。

### 6.2 校验顺序

1. `shiji.oss.enabled` 且 OSS Bean 存在；否则 `OSS_NOT_CONFIGURED`（20004）。
2. `file` 非空；否则 `FILE_EMPTY`（20001）。
3. `size <= max-file-size-bytes`；否则 `FILE_TOO_LARGE`（20002）。
4. `Content-Type` 规范化后落入白名单；否则 `FILE_TYPE_NOT_ALLOWED`（20003）。

### 6.3 Object Key 与 URL

- **Key 规则**：`users/{userId}/meals/{yyyyMMdd}/{uuid}.{ext}`  
  - 日期为 **Asia/Shanghai** 的 `yyyyMMdd`。  
  - `uuid` 为无连字符 UUID。  
  - 扩展名：优先原始文件名；否则按 MIME 映射为 `jpg` / `png` / `webp`。
- **公网 URL**：`FilePublicUrl.build` → `https://{bucket}.{endpointHost}/{编码后的路径段}`（分段 URL 编码）。

### 6.4 OSS 与事务边界

- 使用 `ObjectMetadata` 设置 `ContentLength`、`ContentType`。
- **先 `putObject` 成功，再 `fileAssetRepository.save`**（`@Transactional` 包裹整段业务）。
- OSS 抛 `OSSException` / `ClientException` 或读流 `IOException` → `OSS_UPLOAD_FAILED`（20005），日志带 `userId` 与 `objectKey`。
- **DB 失败**：当前实现若 save 抛错，事务回滚但 OSS 对象已存在 → 形成孤儿对象；缓解策略见设计文档：**日志 + 后续定时清理任务**（首期可简化）。

### 6.5 落库字段（核心）

| 字段 | 本期写入 |
|------|----------|
| `storage_provider` | `aliyun_oss` |
| `upload_source` | `backend_proxy` |
| `status` | `uploaded` |
| `biz_type` | `meal_photo` |
| `etag` | PutObject 返回 ETag（去掉引号） |
| `object_key_hash` | `ObjectKeyHash.sha256Hex(bucket, objectKey)` |

`content_hash`、`thumbnail_url`、`pending` 等列为表预留能力，本期可不填。

### 6.6 配置项（application）

| 配置键 | 说明 |
|--------|------|
| `shiji.oss.enabled` | `true` 才注册 `OSS` Bean |
| `shiji.oss.endpoint` | 如 `https://oss-cn-hangzhou.aliyuncs.com` |
| `shiji.oss.region` | 如 `oss-cn-guangzhou` |
| `shiji.oss.bucket` | 目标 Bucket |
| `shiji.oss.access-key-id` / `access-key-secret` | 建议 `${OSS_*}` 环境变量 |
| `shiji.oss.max-file-size-bytes` | 默认 10485760（10MB） |

`spring.servlet.multipart.max-file-size` / `max-request-size` 需 **不小于** 业务单文件上限（当前 10MB / 11MB）。

---

## 7. 备选方案对比

| 维度 | 后端代理 PutObject（本期） | 先插 DB `pending` 再上传 |
|------|---------------------------|---------------------------|
| 实现复杂度 | 低 | 中（多状态机） |
| 一致性 | 可能 OSS 有、DB 无（可运维补偿） | 可能 DB pending 长期悬挂 |
| MVP 交付 | 快 | 慢 |

**推荐方案**：先 OSS 后 DB（本期实现）。

**不采纳「先 DB 再 OSS」**：多一次状态迁移，MVP 不必要。

---

## 8. 容量与性能

| 项目 | 说明 |
|------|------|
| 瓶颈 | 应用入站带宽与连接数随上传量上升 |
| 缓解 | 单文件上限、全局限流（可后续在网关层加）、**二期直传** |
| 压测 | 上线前按峰值 QPS × 平均文件大小估算 ingress |

---

## 9. 安全与合规

| 项 | 措施 |
|----|------|
| 凭证 | 仅服务端；环境变量注入 |
| 权限 | RAM 最小权限；禁止客户端持有长期 AK |
| 审计 | 服务端日志关联 `userId`、object key |
| 私桶 | 若 Bucket 私有，当前返回的 `url` 可能不可匿名访问 → 需改为签名 URL 或 CDN，**见未决问题** |

---

## 10. 风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| 服务端带宽/CPU 瓶颈 | 高（规模上量后） | 限流、扩容；二期 STS 直传 |
| 密钥泄露 | 高 | 环境变量、RAM、轮换流程 |
| DB 与 OSS 不一致 | 中 | 孤儿对象清理任务；监控 save 失败率 |
| 公网 URL 与 ACL 不匹配 | 中 | 产品与基础设施定公共读/私有+签名 |

---

## 11. 扩展性与演进

- **二期 STS 直传**：沿用 `file_asset`；`upload_source=client_direct`；`object_key` 前缀策略与本期一致即可对齐生命周期与权限模板。
- **完成确认 / 回调**：直传后可增加 `pending → uploaded` 与 HeadObject 校验。
- **内容去重**：可启用 `content_hash`（文件字节 SHA-256），与设计 Open Question 一致。

---

## 12. 实施与运维

| 项 | 说明 |
|----|------|
| 数据库 | 执行 `create_file_asset.sql`（若尚未执行） |
| 部署 | 配置 OSS 环境变量，`enabled=true` |
| 回滚 | 关 `enabled`、下线路由（若需）；已上传 OSS 对象保留或脚本清理 |
| 本地开发 | `enabled=false` 时接口明确失败，避免误连生产 |

---

## 13. 附录

### 13.1 业务错误码（文件模块）

| code | 含义 |
|------|------|
| 20001 | 未选择文件 |
| 20002 | 文件过大 |
| 20003 | 类型不允许 |
| 20004 | OSS 未配置 |
| 20005 | 上传失败 |

### 13.2 代码与规范索引

- 实现包：`services/api/src/main/java/com/shiji/api/modules/file/`
- OpenSpec 设计：`openspec/changes/api-oss-proxy-upload-v1/design.md`
- 增量 Spec：`openspec/changes/api-oss-proxy-upload-v1/specs/file-upload/spec.md`

### 13.3 未决问题

| 编号 | 问题 |
|------|------|
| O1 | 生产 Bucket **公共读** 还是 **私有 + CDN/签名 URL**（决定 `url` 字段是否长期有效） |
| O2 | 是否在首版写入 `content_hash` 做去重 |
