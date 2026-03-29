## MODIFIED Requirements

### Requirement: 接口返回统一结构

所有 API MUST 使用 `com.shiji.api.common.web.ApiResponse` 作为对外 JSON 响应根类型；所有对外 Controller 方法返回类型 MUST 为 `ApiResponse<?>`。响应 MUST 包含字段 `code`、`message`、`data`。成功时 `code` MUST 为 0；失败时 `code` MUST 为非 0 业务码。所有应反馈给客户端的异常 MUST 由全局 `@ControllerAdvice` 统一转换为上述 `ApiResponse` 形状，MUST NOT 依赖未约定的默认错误页或非统一 JSON。

#### Scenario: 接口返回数据

- **WHEN** 客户端调用业务接口且处理成功
- **THEN** 响应体 MUST 为如下结构的 JSON，且字段名与语义保持一致：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

#### Scenario: 接口返回业务错误

- **WHEN** 客户端调用业务接口且因可预期业务规则失败
- **THEN** 响应体 MUST 仍为上述三字段结构，且 `code` MUST 为非 0、`message` MUST 说明失败原因、`data` MAY 为 null 或规范允许的结构化信息
