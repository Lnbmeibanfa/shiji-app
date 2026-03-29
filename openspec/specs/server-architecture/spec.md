# 服务端架构规范（server-architecture）

本规范定义食迹后端服务架构规则。

---

## 需求：服务端负责业务编排

所有业务逻辑必须在后端实现。

### 场景：AI饮食分析

- 当用户提交饮食记录
- 后端负责数据校验
- 后端调用 AI 服务
- 后端返回分析结果

---

## 需求：服务端采用分层架构

后端必须采用标准分层结构。认证相关能力 MUST 落在 `modules/auth` 的 Controller / Service / Repository 分层中；MUST NOT 将认证业务规则散落在无关模块或仅依赖 Controller 内联实现。

### 场景：新增接口

- Controller 负责接收请求
- Service 负责业务逻辑
- Repository 负责数据库访问
- 外部服务调用必须封装在独立模块

---

## 需求：接口返回统一结构

所有 API MUST 使用 `com.shiji.api.common.web.ApiResponse` 作为对外 JSON 响应根类型；所有对外 Controller 方法返回类型 MUST 为 `ApiResponse<?>`。响应 MUST 包含字段 `code`、`message`、`data`。成功时 `code` MUST 为 0；失败时 `code` MUST 为非 0 业务码。所有应反馈给客户端的异常 MUST 由全局 `@ControllerAdvice` 统一转换为上述 `ApiResponse` 形状，MUST NOT 依赖未约定的默认错误页或非统一 JSON。

### 场景：接口返回数据

- **WHEN** 客户端调用业务接口且处理成功
- **THEN** 响应体 MUST 为如下结构的 JSON，且字段名与语义保持一致：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 场景：接口返回业务错误

- **WHEN** 客户端调用业务接口且因可预期业务规则失败
- **THEN** 响应体 MUST 仍为上述三字段结构，且 `code` MUST 为非 0、`message` MUST 说明失败原因、`data` MAY 为 null 或规范允许的结构化信息
