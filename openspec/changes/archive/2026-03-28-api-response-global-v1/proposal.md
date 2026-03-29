## Why

认证模块已引入全局 `ApiResponse` 封装，但规范层面尚未强制「所有 Controller 统一返回类型」与「异常经全局 `@ControllerAdvice` 转为同一外壳」。前端与客户端需要稳定的 HTTP 契约，避免各模块各自返回裸 DTO 或混用异常形态。

## What Changes

- 规范强制：后端所有 Controller 公开接口的返回类型 MUST 为 `ApiResponse<?>`（或等价的全局封装类型）。
- 规范强制：可预期的业务异常与通用异常 MUST 通过全局 `@ControllerAdvice` 统一转换为 `ApiResponse.error(...)`（或 `code`/`message` 语义等价的工厂方法）。
- 明确与现有 `ErrorCode` 分段、校验失败（`MethodArgumentNotValidException` 等）、安全未认证响应的对齐策略。
- 将 `com.shiji.api.common.web.ApiResponse` 确立为项目级事实来源；各模块业务错误码可独立枚举但 MUST 实现 `ErrorCode`（或规范规定的等价契约）。

## Capabilities

### New Capabilities

- `api-http-contract`：定义 HTTP 层统一返回体、Controller 返回类型约束、全局异常处理与错误码映射规则。

### Modified Capabilities

- `server-architecture`：在「接口返回统一结构」需求上补充 MUST：返回体使用全局 `ApiResponse`，异常经全局 `@ControllerAdvice` 输出同一 JSON 形状。

## Impact

- 影响所有未来与现有 `services/api` 的 `Controller` 及异常处理类。
- 影响 Spring Security `AuthenticationEntryPoint` / `AccessDeniedHandler`（若存在）与全局异常处理的一致性。
- 影响前端错误解析与联调文档。
