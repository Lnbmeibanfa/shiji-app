## Context

仓库已在 `com.shiji.api.common.web` 提供 `ApiResponse` 与 `ErrorCode`，认证模块已部分使用。尚未在规范层面对「全项目 Controller 返回类型」与「异常统一出口」形成强制约束，存在后续模块各自实现返回体或分散异常处理的风险。

## Goals / Non-Goals

**Goals:**

- 建立单一 HTTP JSON 响应契约，前端可统一解析。
- 通过全局 `@ControllerAdvice` 收敛异常到 `ApiResponse.error(...)`，减少 Controller 内重复 try/catch。
- 与 Spring Security 未认证等场景对齐为同一 JSON 形状（在可行范围内）。

**Non-Goals:**

- 不在本次定义完整错误码分段表（可后续独立变更）。
- 不强制引入新的第三方库；优先使用 Spring MVC 与现有 `ApiResponse`。

## Decisions

### 1) 单一全局异常处理器为主，领域模块可保留次要 Advice

- 决策：在 `common`（或 `config`）提供主 `GlobalExceptionHandler`，统一处理 `ErrorCode` 承载的业务异常、参数校验异常、通用 `Exception` 兜底。
- 原因：避免多个 `@ControllerAdvice` 行为冲突与重复 JSON 拼装。
- 备选：每模块一个 Advice — 易分裂契约，不利于前端统一。

### 2) 业务异常 MUST 可映射为 `ErrorCode`

- 决策：领域业务异常携带 `ErrorCode`（或可由处理器映射到 `code`/`message`），处理器调用 `ApiResponse.error(ErrorCode)`。
- 原因：与现有 `AuthErrorCode` 一致，可扩展到其他模块枚举。

### 3) 校验异常使用固定业务码或通用参数错误码

- 决策：`MethodArgumentNotValidException` / `ConstraintViolationException` 映射为统一 `ApiResponse`，`code` 使用全局约定值（例如单独一段 4xxxx 或由 `common` 枚举定义），`message` 可汇总首条或列表（需在实现时固定一种策略）。
- 原因：避免 400 默认 HTML/空 body 与项目 JSON 契约不一致。

### 4) Spring Security 入口与全局契约对齐

- 决策：`AuthenticationEntryPoint` / 必要时的 `AccessDeniedHandler` 输出与 `ApiResponse` 相同字段名的 JSON（可直接复用 `ObjectMapper` 序列化 `ApiResponse`）。
- 原因：未登录与业务错误解析路径一致。

## Risks / Trade-offs

- [多个 `@ControllerAdvice` 顺序导致吞掉异常] → 主处理器集中、子模块仅处理极窄异常并谨慎使用 `@Order`。
- [兜底 `Exception` 泄露内部信息] → 生产环境对 `message` 泛化，详细日志仅写服务端。
- [与 Spring 默认错误页混用] → 关闭或统一 `/error` 行为（按实现阶段评估）。

## Migration Plan

1. 按规范新增/调整全局 `GlobalExceptionHandler` 与安全入口 JSON。
2. 逐个模块将 Controller 返回类型改为 `ApiResponse<?>`。
3. 删除或收敛模块内重复异常处理器。
4. 联调验证：成功路径、业务错误、校验错误、未登录。

## Open Questions

- 参数校验错误使用单一 `code` 还是字段级 `data` 结构（例如 `errors: [{field, message}]`）？
- HTTP 状态码策略：是否一律 200 + `code` 区分，还是 4xx/5xx + 仍带 `ApiResponse` body？
