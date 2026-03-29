# 客户端架构规范 — 增量（client-architecture）

本文件为 `openspec/specs/client-architecture/spec.md` 的 **ADDED** 增量，随变更 `flutter-client-infrastructure-v1` 归档后合并。

---

## ADDED Requirements

### Requirement: 网络访问不得绕过 Repository 与 ApiClient

`features/` 下的页面与 Widget MUST NOT 直接依赖 `package:dio` 或原始 HTTP 客户端发起后端调用。所有后端 API 调用 MUST 通过 `core/network` 暴露的统一客户端及 `features/<feature>/repositories` 中的 Repository 完成。

#### Scenario: 新增业务页面

- **WHEN** 开发者在某 feature 中新增页面需要拉取数据
- **THEN** 页面调用 Repository 或上层状态对象，而非直接 `dio.get/post`

### Requirement: Token 不得在业务代码中散落

业务 feature MUST NOT 直接读写 token 字符串常量或调用 `flutter_secure_storage` 存取 token。会话读写 MUST 经 `AuthStorage`（或项目统一命名之会话封装）与认证控制器协调。

#### Scenario: 保存登录结果

- **WHEN** 登录 API 返回 token
- **THEN** token 写入由 `AuthStorage`（或等价封装）执行，页面代码不直接持久化密钥字符串
