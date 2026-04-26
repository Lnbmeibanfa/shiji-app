## 1. 依赖与配置

- [x] 1.1 在 `services/api/pom.xml` 添加 `com.alibaba:dashscope-sdk-java` 依赖（版本与官方 Maven 文档一致或项目内统一）
- [x] 1.2 新增 `@ConfigurationProperties` 类（如 `DashScopeProperties`），包含 `api-key`（映射 `${DASHSCOPE_API_KEY}`）、`text-model`、超时等字段，并在主应用类或配置类上启用 `@EnableConfigurationProperties`
- [x] 1.3 在 `application.yml`（及可选 `application-local.yml.example`）中增加 `dashscope.api-key: ${DASHSCOPE_API_KEY}` 与默认 `text-model`，**不**提交真实密钥

## 2. Prompt 资源与客户端

- [x] 2.1 在 `src/main/resources/prompts/`（或 design 约定路径）新增默认连通性短文本文件（如单行「你好」），供未传 `message` 时读取
- [x] 2.2 实现 `DashScopeTextClient`（或等价命名）：封装 DashScope 文本 `Generation` 调用，入参为用户消息字符串，返回模型文本与所用模型名；对 SDK 异常转换为统一业务异常或 `ApiResponse` 错误码
- [x] 2.3 当未配置 API Key 时行为与设计一致：接口返回明确错误（推荐），避免启动失败阻断无密钥的 CI

## 3. HTTP API

- [x] 3.1 新增请求 DTO（可选字段 `message`）与响应 DTO（含 `reply`、可选 `model`）
- [x] 3.2 新增 `POST /api/ai/dashscope/ping` Controller，使用 `AuthPrincipal` 获取当前用户；**不**将路径加入 `SecurityConfiguration` 的 `permitAll`
- [x] 3.3 使用 `ApiResponse` 包装成功与失败体，与现有 API 风格一致

## 4. 测试与验证

- [x] 4.1 为 `DashScopeTextClient` 或 Controller 增加单元测试：Mock SDK 或客户端，断言请求/响应与鉴权行为
- [x] 4.2 在 `README` 或 `services/api/README.md` 中补充本地设置 `DASHSCOPE_API_KEY` 与调用示例（curl），标注为联调用途
- [x] 4.3 本地手动验证：登录后调用接口，确认返回模型原文
