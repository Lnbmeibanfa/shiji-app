# 设计说明：Flutter 客户端基础设施

## Context

- **现状**：`apps/mobile` 已有 `core/theme/*`、`core/widgets/*`、`features/home` 占位；`main.dart` 为单页占位，无网络、无路由、无持久化会话。
- **约束**：`openspec/specs/client-architecture/spec.md`（feature-first、禁直调 AI、禁页面乱写 hex）、`design-tokens` 规范；后端统一 `ApiResponse`（见 `services/api/docs/auth-api.md`）。
- **目标用户**：本机与团队联调；MVP 阶段**避免过重框架**。

## Goals / Non-Goals

**Goals:**

- 支撑**登录闭环**所需：发码、登录、存 token、受保护路由、登出清理。
- 建立**单一 ApiClient 出口**与**统一响应解析**。
- 建立**启动门禁**（已登录进首页，未登录进登录页）。
- 建立 **P1** 级可维护性：统一错误提示、loading 约定、调试日志、轻量状态管理选型。

**Non-Goals:**

- 不实现具体登录页 UI（可由后续 `features/auth` 页面变更完成）。
- 不引入 AI SDK、不配置埋点/CI/i18n/崩溃监控（列为 P2）。
- 不强制 `json_serializable`（首版可手写 `fromJson`，见决策节）。

## 目录结构（推荐）

```
lib/
  main.dart
  core/
    config/           # AppConfig：dart-define 读取的 baseUrl、环境名、超时
    network/          # ApiClient、拦截器、ApiResponse/ApiException、Dio 单例或注册
    storage/          # SecureStorageFacade、PrefsFacade、AuthStorage（仅对外暴露封装）
    routing/          # app_router.dart：GoRouter、routes、redirect
    feedback/         # AppMessenger / AppSnackBar 薄封装（可选 app_toast.dart）
    logging/          # AppLogger 包装（debug 打印、release 降级）
  features/
    auth/             # 认证业务（与基础设施衔接）
      data/           # DTO、API 路径常量（可选）
      models/         # LoginResponse 等（手写 fromJson）
      repositories/   # AuthRepository：调 ApiClient，不暴露 Dio
      services/       # 可选：组合 repository 与 AuthStorage
    home/
      ...
```

**职责简述：**

| 路径 | 职责 |
|------|------|
| `core/config` | 唯一读取 `--dart-define` 与环境枚举，供 `network` 使用。 |
| `core/network` | 唯一持有 Dio；解析 JSON 为 `ApiResponse<T>`；`code==0` 返回 data；否则抛 `ApiBusinessException`（携带 code/message）。 |
| `core/storage` | 封装密钥与 prefs；`AuthStorage` 提供 `readToken`/`writeToken`/`clear`。 |
| `core/routing` | 声明 Route 路径、Shell、**redirect**：读 `AuthController` 或 `AuthStorage` 判断是否已登录。 |
| `core/feedback` | 统一 SnackBar 样式（可走 Theme，不引入新色值）。 |
| `features/*/repositories` | 各 feature 对后端 API 的唯一调用层；**页面只调 Repository 或上层 Notifier**。 |
| `features/auth` | 登录相关 model、auth_api（路径+body 构造）、AuthRepository、后续可与 `AuthController` 同 feature 或放 `core`（见决策）。 |

## Decisions

### 1. HTTP：dio 相对 http

- **选择**：**dio**。
- **理由**：拦截器集中注入 token、统一错误与日志、超时与取消更顺手；与「单一 ApiClient」模型契合。
- **备选**：`http` 包更小，但拦截与中间层需自写，重复劳动多。

### 2. 路由：go_router

- **选择**：**go_router**。
- **理由**：`redirect` 与声明式路由适合「登录门禁」；与 Flutter 团队文档一致。
- **备选**：Navigator 2 手写或 auto_route（codegen 更重，MVP 不急）。

### 3. 状态管理：首版轻量

- **选择**：**Riverpod**（`flutter_riverpod`）或 **Provider** 二选一；推荐 **Riverpod** 便于测试与覆盖 AuthController。
- **若极简**：可用 `ChangeNotifier` + 手动 `Provider`，但团队扩展时易乱。
- **非目标**：首版不引入 **Bloc** 全家桶。

### 4. JSON：json_serializable

- **首版**：**手写** `fromJson`/`toJson` 于少量 model（登录响应、ApiResponse 包装）。
- **后续**：模型膨胀后再加 `json_serializable` + build_runner。

### 5. AuthController 放置

- **推荐**：`AuthController`（或 `SessionNotifier`）放在 **`core/` 或 `features/auth`** 均可；若多 feature 依赖会话，放 **`lib/core/auth/auth_controller.dart`** 更直观，由 Riverpod 提供全局 scope。

### 6. token 注入方式

- Dio 拦截器内异步读取 `AuthStorage.readToken()`，若非空则设 `Authorization: Bearer <token>`。

## 核心模块设计（仅设计，无代码）

### 3.1 网络层（ApiClient）

- 使用 **dio** 单例或 Riverpod 注入的 `Dio`。
- **baseUrl**：来自 `AppConfig.apiBaseUrl`（由 `--dart-define=API_BASE_URL=...` 注入，缺省可为开发机占位）。
- **timeout**：连接/接收各 15s～30s（写入配置常量）。
- **token**：请求拦截器调用 `AuthStorage`，附加 Bearer。
- **响应**：`Response.data` 为 Map 时解析为 `ApiResponse.fromJson`；**`code == 0`** 时取 `data`；**`code != 0`** 抛 `ApiBusinessException(code, message)`。**HTTP 4xx/5xx** 抛 `ApiHttpException` 或映射为统一异常，由 feedback 层提示。
- **异常**：业务层只捕获「统一异常类型」，不在页面解析原始 Map。

### 3.2 API 层

- **禁止** `Widget` / `Page` 直接 `import 'package:dio/dio.dart'` 并发起请求。
- 每 feature 可定义 `xxx_api.dart`（仅 path 与 query 常量）+ **`XxxRepository`** 调用 `ApiClient` 暴露的方法（如 `postJson`）。
- **Service** 可选：当 repository 需组合多接口或事务式编排时使用。

### 3.3 环境配置

- 构建命令示例：`flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080 --dart-define=APP_ENV=dev`。
- **dev / staging / prod**：`APP_ENV` 枚举 + 可选不同默认 baseUrl（仅默认值，生产仍以 define 或 CI 注入为准）。
- **禁止**在 Dart 源码中写死生产域名。

### 3.4 存储层

- **token**：`flutter_secure_storage`。
- **非敏感**：`shared_preferences`（如「是否看过引导」）。
- **AuthStorage**：唯一写 token 的入口；登出时 `clear`。

### 3.5 路由系统

- **go_router**：`routes` 含 `/login`、`/home`（或 `/`）；**ShellRoute** 可后续再加。
- **redirect**：`(context, state)` 中若已登录且目标为 `/login` → 重定向 `/home`；未登录且目标需认证 → `/login`。

### 3.6 认证状态（AuthController）

- 启动：`readToken` 非空则标记 `authenticated`（可选再调 `session/restore` 校验，属后续优化）。
- 登录成功：`writeToken` + `notifyListeners`/Riverpod update。
- 登出：`clear` + 导航到登录页。

### 3.7 全局反馈

- 封装 `showAppSnackBar(context, message)`，错误用短文案（`message` 来自异常）。
- **策略**：`ApiBusinessException` → SnackBar；网络断开 → 固定「网络异常」文案。

### 3.8 loading

- **按钮**：扩展 `ShijiButton` 或包一层 `isLoading` + 禁用点击（设计在 UI 变更中落地）。
- **页面**：`Overlay` 或 `showDialog` 屏障可选；首版可用 **全屏 `CircularProgressIndicator`** 封装在 `core/feedback`。

## 与后端契约对齐

- 所有接口 JSON 外层：**`{ "code": int, "message": String, "data": T? }`**；**`code != 0` 可能伴随 HTTP 200**。
- 登录 body 须含 **`agreements`**：`[{ "agreementType", "agreementVersion", "accepted": true }]`，与 UI 勾选一致：仅当用户勾选协议后，构造上述数组并提交；**禁止**「UI 未勾选但请求体传 accepted true」。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| Android  cleartext HTTP 调试失败 | 文档中说明 `network_security_config`；优先 HTTPS 或定义域白名单。 |
| redirect 与 Riverpod 异步读 token 竞态 | 启动屏或 `refreshListenable` 与 `AuthController` 同步。 |
| 双端 secure_storage 行为差异 | 封装层统一异常，登录失败时提示重试。 |

## Migration Plan

- 合并后：`main.dart` 改为 `ProviderScope` + `MaterialApp.router`；占位首页挂到 `/home`，新增 `/login` 占位直至登录 UI 变更完成。
- 回滚：保留 git 分支；依赖可逆。

## Open Questions

- 是否在首版就调用 `/api/auth/session/restore` 校验 token（增加一次启动请求）？
- 协议版本号 `agreementVersion` 是否由远程配置下发（后续）？

## json_serializable 引入阈值（ADR 摘要）

- **当前**：少量 DTO（如 `LoginResponse`）手写 `fromJson` 即可。
- **建议引入** `json_serializable` + `build_runner` 的时机：当 `features/` 下累计超过约 **10** 个请求/响应模型、或同一模型在多处重复手写解析时，再开变更专门接入代码生成，避免 MVP 阶段构建链与生成文件噪音。
