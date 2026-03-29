# API HTTP 统一契约（api-http-contract）

本规范定义 Spring MVC 对外 JSON 与 `ApiResponse` / `ErrorCode` 的全局约定。

---

## 规范要求

### Requirement: Controller 对外返回类型必须为 ApiResponse

所有对外暴露的 Spring MVC Controller 方法（含未来新增模块）MUST 将返回类型声明为 `com.shiji.api.common.web.ApiResponse<?>`（或经 OpenSpec 归档后指向的等价全局类型）。Controller MUST NOT 直接返回裸实体、裸 DTO、`Map`、`String` 等作为对外 JSON 根对象（除非规范明确豁免的基础设施端点）。

#### Scenario: 成功调用返回统一外壳

- **WHEN** 客户端调用任意业务接口且处理成功
- **THEN** HTTP 响应体 MUST 为 `ApiResponse` JSON，包含 `code`、`message`、`data`，且 `code` MUST 为 0

#### Scenario: 业务失败仍返回统一外壳

- **WHEN** 服务端因可预期业务规则拒绝请求并被全局异常处理捕获
- **THEN** HTTP 响应体 MUST 仍为 `ApiResponse` JSON，且 `code` MUST 为非 0 业务码、`message` MUST 为可展示说明

### Requirement: 异常 MUST 经全局 ControllerAdvice 转为 ApiResponse

所有应返回给客户端的业务异常与常见框架异常 MUST 由全局 `@ControllerAdvice` 统一转换为 `ApiResponse.error(...)`（或与 `code`/`message` 语义等价的工厂方法）。模块级 `@ControllerAdvice` 仅允许在不影响全局契约一致性的前提下补充极窄场景，且 MUST NOT 改变统一 JSON 字段名与含义。

#### Scenario: 业务异常映射为 ErrorCode

- **WHEN** 服务抛出携带 `ErrorCode`（或规范等价契约）的业务异常
- **THEN** 全局处理器 MUST 返回 `ApiResponse`，其 `code`/`message` MUST 与该 `ErrorCode` 一致

#### Scenario: 参数校验失败被统一封装

- **WHEN** 请求体验证失败并抛出 Spring 校验相关异常
- **THEN** 全局处理器 MUST 返回 `ApiResponse` 形状，且 MUST 使用项目约定的校验错误 `code` 与可读 `message`（实现阶段固定一种汇总策略）

### Requirement: 业务错误码通过 ErrorCode 扩展

各业务模块 MAY 定义本模块错误码枚举，MUST 实现 `com.shiji.api.common.web.ErrorCode`（或规范规定的等价接口），以便 `ApiResponse.error(ErrorCode)` 统一封装。不同模块 MAY 使用不同数值区间，但 MUST 避免与全局保留码冲突（保留区间由后续规范或代码枚举统一维护）。

#### Scenario: 认证模块错误码可被封进统一响应

- **WHEN** 认证流程返回 `AuthErrorCode` 中定义的错误
- **THEN** 响应体 MUST 仍为 `ApiResponse`，且 `code` MUST 取自该 `ErrorCode`
