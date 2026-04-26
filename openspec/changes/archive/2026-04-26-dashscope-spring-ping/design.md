## Context

后端为 Spring Boot（`services/api`），安全模型为除 `/api/auth/**` 部分路径外**默认需登录**。项目已存在 `ApiResponse` 统一响应与 OSS 等阿里云组件，但尚未集成 DashScope。用户已确定使用环境变量 **`DASHSCOPE_API_KEY`**，并希望在 `application-*.yml` 中通过 **`${DASHSCOPE_API_KEY}`** 绑定到 Spring 配置属性。

## Goals / Non-Goals

**Goals:**

- 提供最小 HTTP 接口：调用 DashScope **文本生成**能力，发送短文本（默认或请求体指定），返回模型输出文本。
- 使用官方 **`dashscope-sdk-java`**（`Generation` 或当前文档推荐的文本调用方式）完成调用。
- 配置项包含：API Key（来自环境变量占位符）、默认模型名、合理超时（连接/读取）。
- 满足既有 **`ai-capability`** 规范：与用户可见文案相关的提示词以**仓库内资源文件**维护（本变更为单一句子级连通性，使用极短 `prompts` 资源即可）。

**Non-Goals:**

- 图像多模态、餐食识别、落库 `meal_recognition_*`。
- 提示词工程、内容安全过滤的完整实现（可在后续迭代补强）。
- 对外公开匿名调用该接口（默认走现有 Session 鉴权）。

## Decisions

1. **SDK 与 API 形态**  
   - **选用** `dashscope-sdk-java` 调用文本模型（如 `qwen-turbo` 或文档当前推荐的轻量模型名，以可配置属性为准）。  
   - **理由**：与阿里云示例一致、错误与鉴权行为可预期；HTTP 手写请求留作备选，不作为本变更首选。

2. **配置与密钥**  
   - 在 `application.yml`（或 `application-local.yml` 示例）中使用 `dashscope.api-key: ${DASHSCOPE_API_KEY}`（属性名可用 `apiKey` 的 kebab 形式映射到 `DashScopeProperties`）。  
   - **理由**：不在仓库中存放明文 Key；与 Spring `@ConfigurationProperties` 一致。若本地未设置变量，应用启动策略：**可选**在实现时允许缺省仅影响该 Bean 懒加载，或启动即失败——实现阶段在 tasks 中二选一并写清（推荐缺 Key 时接口返回明确业务错误码而非启动失败，便于 CI 无密钥跑通其他测试）。

3. **HTTP 契约**  
   - 路径建议：`POST /api/ai/dashscope/ping`（与其它 `/api/*` 一致，且需登录）。  
   - 请求体（JSON）：可选字段 `message`（字符串）；缺省时使用 prompts 文件中默认短句（如「你好」）。  
   - 响应：复用 `ApiResponse`，数据体包含 `reply`（模型原文）及可选 `model`（实际使用的模型名）。  
   - **备选**：`GET` 仅用于健康检查且不调 DashScope——本变更以 **真实调用** 为准，不设无密钥 GET。

4. **模块边界**  
   - 新增包如 `com.shiji.api.modules.ai`：`DashScopeProperties`、`DashScopeTextClient`（封装 SDK）、`DashScopePingController`（或 `AiPingController`）。  
   - **理由**：与后续识别/总结共用同一客户端与配置。

5. **安全**  
   - 接口**不**加入 `permitAll`；调用方须已登录，降低 Key 被匿名滥用的风险。  
   - 后续可加 profile（如仅 `dev` 开放）或管理角色——本变更不强制，仅在 risks 中记录。

## Risks / Trade-offs

- **[风险] 每次调用产生 token 费用** → 仅登录用户可调用；后续可加频控。  
- **[风险] 模型名变更** → 配置化 `dashscope.text-model`。  
- **[风险] CI 无 `DASHSCOPE_API_KEY`** → 集成测试对该接口**排除**或 Mock 客户端；单元测试不连外网。  
- **[权衡] 极简 prompt 与「提示词版本化」** → 使用 `src/main/resources/prompts/...` 单文件存放默认句，变更即版本可追溯。

## Migration Plan

1. 在运行环境（本地、服务器、CI 密钥库）配置 `DASHSCOPE_API_KEY`。  
2. 部署新版本 API。  
3. 使用已登录客户端调用 `POST /api/ai/dashscope/ping` 验证返回。  
4. **回滚**：移除该端点或关闭配置即可，无数据迁移。

## Open Questions

- 默认文本模型名以团队百炼控制台**当前可用**名为准（在 `application.yml` 给默认值，可在 tasks 里写「与官方 quickstart 对齐」）。  
- 是否在 OpenAPI/文档中标注该接口为「联调/诊断」用途（建议标注，避免产品误用为正式功能）。
