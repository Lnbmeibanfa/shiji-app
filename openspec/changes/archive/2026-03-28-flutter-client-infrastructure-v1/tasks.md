# 任务清单：Flutter 客户端基础设施



## 1. P0 — 依赖与工程配置



- [x] 1.1 在 `pubspec.yaml` 添加 dio、flutter_secure_storage、shared_preferences、go_router、flutter_riverpod（版本与 `design.md` 决策一致）

- [x] 1.2 实现 `core/config`：`AppEnv` 枚举与 `AppConfig.apiBaseUrl`（`--dart-define=API_BASE_URL`、`APP_ENV`），禁止硬编码生产域名

- [x] 1.3 文档化本地运行示例（含 Android 模拟器访问 `10.0.2.2` 的说明，指向 `design.md` 或 README 片段）



## 2. P0 — 网络层与契约



- [x] 2.1 定义 `ApiResponse<T>` 与业务异常类型（如 `ApiBusinessException`、`ApiHttpException`），解析 `{ code, message, data }`

- [x] 2.2 基于 Dio 实现 `ApiClient`：baseUrl、超时、拦截器挂点

- [x] 2.3 实现响应统一解析：`code == 0` 返回 data；`code != 0` 抛出业务异常（不依赖 HTTP 状态码）

- [x] 2.4 单元测试：模拟 JSON 业务失败（HTTP 200 + code 非 0）与成功路径



## 3. P0 — 存储与 Auth 状态



- [x] 3.1 封装 `SecureStorageFacade` / `PrefsFacade`（薄封装即可）

- [x] 3.2 实现 `AuthStorage`：read/write/clear token，为唯一 token 持久化出口

- [x] 3.3 实现 `AuthController`（或 `SessionNotifier` + Riverpod）：启动时恢复 token、登录写入、登出清除

- [x] 3.4 Dio 请求拦截器：从 `AuthStorage` 读取 token 并注入 `Authorization: Bearer`



## 4. P0 — 路由与启动门禁



- [x] 4.1 定义 `GoRouter`：`/login`、`/home`（或主 shell）路径常量

- [x] 4.2 实现 `redirect`：未登录 → 登录；已登录访问登录页 → 首页（行为以 spec 为准）

- [x] 4.3 修改 `main.dart`：`ProviderScope` + `MaterialApp.router`，占位登录页与现有 home 占位衔接

- [x] 4.4 验证冷启动：有 token 时进入首页路径；无 token 时进入登录路径



## 5. P0 — Feature 侧衔接（无 UI 细节）



- [x] 5.1 在 `features/auth` 建立 `AuthRepository`：调用发码、登录 API（路径与 `services/api/docs/auth-api.md` 一致）

- [x] 5.2 登录请求体构造 `agreements` 数组，与后续 UI 勾选逻辑对接（可先由 Repository 接收参数，禁止硬编码 accepted）

- [x] 5.3 登录成功后由 `AuthController` 写 token 并触发路由更新



## 6. P1 — 全局反馈与 Loading



- [x] 6.1 实现 `core/feedback`：统一 SnackBar（或 Toast）方法，错误时展示 `message`

- [x] 6.2 扩展或包装 `ShijiButton`：支持 `isLoading` 与禁用重复点击（样式仍用 Design Token）

- [x] 6.3 可选：全屏 loading 封装，供登录提交期间使用



## 7. P1 — 日志与模型



- [x] 7.1 实现 `AppLogger`：debug 打印请求/错误摘要，release 降级

- [x] 7.2 为登录相关 DTO 手写 `fromJson`（LoginResponse 等），与后端字段对齐

- [x] 7.3 评估后续引入 `json_serializable` 的阈值（文档记入 `design.md` Open Questions 或 ADR）



## 8. P1 — 状态管理收敛



- [x] 8.1 将 `AuthController`、router 刷新与 Riverpod 注册方式文档化（短节即可）

- [x] 8.2 确保业务页面不直接 import `dio`（静态分析或 code review 清单）



## 9. P2 — 后续（本变更不阻塞归档）



- [x] 9.1 CI：`flutter analyze`、`flutter test` 流水线

- [x] 9.2 i18n 占位与 `intl` 依赖策略

- [x] 9.3 崩溃监控 / 埋点选型与隐私说明



## 10. 规范与收尾



- [x] 10.1 对照 `specs/flutter-http-contract/spec.md`、`specs/flutter-client-bootstrap/spec.md` 自检

- [x] 10.2 更新 `apps/mobile` README 或 `docs/` 中的基础设施说明（如何 define、如何跑通登录 API）

- [x] 10.3 准备 OpenSpec 归档（合并 `client-architecture` 增量至主 spec）


