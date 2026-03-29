# Flutter 客户端基础设施（flutter-client-infrastructure-v1）

## Why

当前 `apps/mobile` 已具备 Design Token 与基础 UI 组件，但**缺少业务向基础设施**：无 HTTP 客户端、无统一 API 契约解析、无环境配置、无安全存储、无路由与启动门禁。在此状态下无法可靠落地**真实登录闭环**（发码、登录、持 token 访问、登出），也无法规模化接入后端 `ApiResponse` 体系。本变更在**不重复造 UI Token 与现有组件**的前提下，补齐跨 feature 能力，使后续业务（含登录页）可基于统一契约开发。

## What Changes

- 引入 **HTTP 客户端抽象**（基于 **dio**），集中配置 baseUrl、超时、拦截器与 **Bearer token 注入**。
- 建立 **统一响应模型**：解析后端 `{ "code", "message", "data" }`；**`code != 0` 时抛出可预期业务异常**（即使 HTTP 状态码为 200）。
- 建立 **环境配置**：通过 `--dart-define` 注入 `API_BASE_URL`、环境名（dev/staging/prod），不在仓库硬编码生产地址。
- 建立 **存储层**：`flutter_secure_storage` 存 token；`shared_preferences` 存非敏感偏好；对外仅暴露 **`AuthStorage`** 等封装，**禁止业务散落读写 token 字符串**。
- 建立 **路由**：采用 **go_router**，定义登录/首页等路径，**redirect** 根据登录态切换。
- 建立 **认证状态**：启动时尝试恢复 token；提供登录成功写入、登出清除；与路由门禁联动。
- **P1**：全局 SnackBar/Toast 封装、按钮/页面 loading 约定、轻量日志、手写或最小化 JSON 模型规范。
- **明确不包含（本变更提案范围内不要求落地代码）**：AI 直连、重状态管理框架、埋点、CI、i18n、崩溃监控（可作为 P2 后续变更）。

## Capabilities

### New Capabilities

- `flutter-http-contract`：HTTP 访问、环境变量、统一 `ApiResponse` 解析与异常类型、与后端契约对齐。
- `flutter-client-bootstrap`：AuthStorage、会话状态、go_router 与 redirect、全局反馈与 loading 约定。

### Modified Capabilities

- `client-architecture`：**增量**补充「页面不得直接使用 HTTP 客户端」「跨模块网络访问须经 `core/network` 与 feature repository」等架构约束（详见本变更下 `specs/client-architecture/spec.md` 中的 **ADDED** 条款）。

## Impact

- **依赖**：`apps/mobile/pubspec.yaml` 将增加 dio、flutter_secure_storage、shared_preferences、go_router 等（具体版本以 `design.md` 为准）。
- **代码结构**：`lib/core/` 下新增 `config`、`network`、`storage`、`routing`、`feedback` 等目录；`features/auth/` 在实现登录时可增加 `data/`、`repositories/`、`services/` 等（与现有 feature-first 一致）。
- **后端**：无接口变更；客户端须遵守 `services/api` 已公开的 `ApiResponse` 与登录 `agreements` 结构。
- **设计体系**：不改变 Design Token；新增代码**仅**通过既有 Token/主题 API 使用样式（基础设施无业务 UI 时不受影响）。
