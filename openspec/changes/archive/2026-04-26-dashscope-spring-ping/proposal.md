## Why

接入阿里云 DashScope（百炼）前，需要先验证 API Key、网络与 SDK 调用链可用。当前后端尚未接入大模型依赖与配置，缺少一个可观测的最小闭环，不利于后续多模态识别与总结能力迭代。

## What Changes

- 在 `services/api` 中通过 Spring Boot 暴露**仅用于连通性验证**的 HTTP 接口：向 DashScope 文本模型发送固定或请求体中的短文本（如「你好」），将模型原文返回。
- 在 `application-*.yml` 中使用 **`${DASHSCOPE_API_KEY}`** 注入密钥（由环境/部署提供，不落库、不提交仓库）。
- 新增 **`dashscope-sdk-java`** 依赖，并增加最小配置类（超时、默认文本模型名等，细节见 design）。
- **不**在本变更中实现图像识别、餐食业务编排或持久化识别结果。

## Capabilities

### New Capabilities

- `dashscope-llm-ping`：定义「DashScope 文本连通性」API 的请求/响应与错误语义，作为后续 AI 模块的基础契约。

### Modified Capabilities

- （无）本变更为独立连通性能力，不改变现有 `meal-record`、`file-upload` 等能力的需求级行为。

## Impact

- **代码**：`services/api` 新增 `ai`（或 `llm`）相关配置、客户端封装、Controller、DTO；可选 `prompts/` 下极短模板文件以满足 `ai-capability` 对提示词版本化的要求。
- **依赖**：Maven 引入 `com.alibaba:dashscope-sdk-java`（版本在 design 中固定）。
- **运维**：运行环境需设置 `DASHSCOPE_API_KEY`；本地与 CI 的密钥管理需与现有惯例一致。
- **安全**：接口仅作开发/联调或受控环境使用时的风险需在 design 中说明（例如鉴权、profile 限制），避免裸奔到公网。
