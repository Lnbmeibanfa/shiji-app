# Flutter HTTP 与 API 契约（flutter-http-contract）

本规范定义 `apps/mobile` 与后端 HTTP 交互的分层与行为，适用于所有经后端 `ApiResponse` 封装的接口。

---

## 规范要求

### Requirement: 统一 ApiResponse 解析

客户端 MUST 将成功响应体解析为与后端一致的 `{ "code", "message", "data" }` 结构。当 `code == 0` 时，业务层 MUST 获得 `data` 域反序列化结果。当 `code != 0` 时，客户端 MUST NOT 将响应当作业务成功处理，MUST 抛出或返回可识别的业务错误（携带 `code` 与 `message`），且该情况 MAY 在 HTTP 状态码为 200 时发生。

#### Scenario: 业务成功

- **WHEN** 响应 JSON 中 `code` 为 `0` 且 `data` 可解析为目标类型
- **THEN** 调用方收到解析后的 `data`，不抛出业务异常

#### Scenario: 业务失败

- **WHEN** 响应 JSON 中 `code` 不为 `0`
- **THEN** 调用方收到业务错误信息（至少包含 `code` 与 `message`），且不将本次调用视为成功

### Requirement: 单一 HTTP 出口

所有经后端的 HTTP 调用 MUST 通过 `core/network` 中定义的客户端（如基于 Dio 的 `ApiClient`）发起。业务页面与 Widget MUST NOT 直接创建或持有独立 `Dio` 实例并用于 API 请求。

#### Scenario: Feature 调用后端

- **WHEN** `features/` 下模块需要请求后端
- **THEN** 请求 MUST 经由 Repository（或同等封装）调用 `ApiClient`，而非在 `build` 方法中直接使用 Dio

### Requirement: 环境基地址可注入

API 根地址 MUST 通过构建时注入（如 `--dart-define=API_BASE_URL=...`）或等价机制读取，MUST NOT 在业务源码中硬编码生产环境域名。

#### Scenario: 本地联调

- **WHEN** 开发者使用开发机构建应用
- **THEN** 可通过注入的 `API_BASE_URL` 指向本机或内网网关，无需修改 Dart 业务文件中的常量域名

### Requirement: 鉴权头注入

对需要登录态的接口，客户端 MUST 在请求拦截器中附加 `Authorization: Bearer <token>`，其中 `token` MUST 从安全存储封装（如 `AuthStorage`）读取，MUST NOT 由各业务页面拼接字符串传入网络层。

#### Scenario: 已登录请求

- **WHEN** `AuthStorage` 中存在有效 token 且发起受保护 API
- **THEN** 请求头包含 Bearer token

### Requirement: 超时与错误类型区分

HTTP 客户端 MUST 配置连接与接收超时。网络层 MUST 将 HTTP 状态错误、网络不可用与业务 `code != 0` 区分，以便上层选择提示策略。

#### Scenario: 网络超时

- **WHEN** 请求超过配置的超时时间
- **THEN** 上层可收到可识别的网络类错误，而非与业务码混淆
