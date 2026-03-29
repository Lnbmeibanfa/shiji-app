# Flutter 会话、路由与全局反馈（flutter-client-bootstrap）

本规范定义登录态持久化、路由门禁、全局反馈与 loading 约定，支撑未登录/已登录分流及登录闭环。

---

## ADDED Requirements

### Requirement: Token 安全存储

访问令牌 MUST 使用安全存储（如 `flutter_secure_storage`）读写。业务代码 MUST 通过 `AuthStorage`（或等价封装）访问 token，MUST NOT 在多个文件中分散使用原始 secure storage API 读写 token。

#### Scenario: 登出清除

- **WHEN** 用户执行登出
- **THEN** 安全存储中的 token MUST 被清除，且后续受保护请求不再附加旧 token

### Requirement: 路由登录门禁

应用 MUST 使用声明式路由（如 `go_router`）并提供 `redirect` 逻辑：未登录用户访问需认证路径时 MUST 被导向登录相关路径；已登录用户访问登录页时 MAY 被导向首页或主 shell。

#### Scenario: 未登录访问受保护页

- **WHEN** 无有效会话且用户导航至需认证的 route
- **THEN** 应用显示登录路径对应页面（或等价），而非受保护内容

### Requirement: 启动时恢复会话状态

应用启动时 MUST 从 `AuthStorage` 读取 token 并更新全局认证状态（如 `AuthController`），以便路由 `redirect` 与首屏一致。

#### Scenario: 冷启动已登录

- **WHEN** 设备上已保存 token 且用户冷启动应用
- **THEN** 认证状态为已登录，用户不应被错误地要求再次输入手机号（除非 token 后续校验失败）

### Requirement: 全局错误提示

对可预期的 API 失败（含业务 `code != 0` 与网络错误），应用 MUST 通过统一入口（如 SnackBar 封装）向用户展示简短说明，且文案 MUST 优先使用服务端返回的 `message`（若存在）。

#### Scenario: 业务错误展示

- **WHEN** API 返回 `code != 0` 且 `message` 非空
- **THEN** 用户看到包含该信息的提示，而非无说明的白屏或仅控制台日志

### Requirement: Loading 与重复提交防护

发起登录或发送验证码等异步操作时，触发控件 MUST 进入 loading 或禁用态，直至请求结束，以避免重复提交。

#### Scenario: 登录请求进行中

- **WHEN** 登录请求已发出且未完成
- **THEN** 登录主按钮不可再次触发同一提交

### Requirement: 登录请求与协议勾选一致

调用登录接口时，请求体中的 `agreements` MUST 与用户在 UI 上的勾选结果一致；若用户未勾选协议，客户端 MUST NOT 发送 `accepted: true`，且 MUST NOT 发起登录请求（或 MUST 先提示用户勾选）。

#### Scenario: 未勾选协议

- **WHEN** 用户未勾选协议
- **THEN** 登录操作不被提交，或提交体中不包含已接受协议的不当表述
