# DashScope LLM Ping (dashscope-llm-ping)

## Purpose
定义 DashScope 文本连通性能力的最小可用规范，覆盖认证接口、API Key 注入与默认 prompt 版本化管理。

## Requirements

### Requirement: 认证文本连通性接口
系统 SHALL 提供经认证的 HTTP 接口，使用配置的 DashScope API Key 调用文本大模型，将用户指定或默认短文本发送给模型，并将模型返回文本提供给调用方。

#### Scenario: 已登录用户发送默认短文本
- **WHEN** 已登录用户请求连通性接口且未在请求体中提供自定义消息
- **THEN** 系统 SHALL 使用仓库内 prompts 资源定义的默认短文本作为用户消息调用 DashScope
- **AND** 响应 SHALL 包含模型返回的文本内容

#### Scenario: 已登录用户发送自定义短文本
- **WHEN** 已登录用户在请求体中提供非空的 `message` 字符串
- **THEN** 系统 SHALL 将该字符串作为用户消息调用 DashScope
- **AND** 响应 SHALL 包含模型返回的文本内容

#### Scenario: 未认证用户不得调用
- **WHEN** 未登录客户端请求该连通性接口
- **THEN** 系统 SHALL 返回未授权错误
- **AND** 系统 SHALL NOT 调用 DashScope

### Requirement: DASHSCOPE_API_KEY 配置
系统 SHALL 从 Spring 配置属性读取 DashScope API Key，该属性值 SHALL 通过环境变量 `DASHSCOPE_API_KEY` 注入（例如在 YAML 中使用 `${DASHSCOPE_API_KEY}`），且 SHALL NOT 将密钥硬编码在源代码或默认配置文件中。

#### Scenario: 环境变量提供密钥
- **WHEN** 运行环境已设置 `DASHSCOPE_API_KEY`
- **THEN** 应用 SHALL 能够使用该值初始化 DashScope 客户端并完成一次成功文本调用（在密钥有效且服务可达的前提下）

### Requirement: 默认 prompt 文件化管理
系统 SHALL 将未提供 `message` 时使用的默认用户消息存放在代码仓库资源文件中（例如 `src/main/resources/prompts/` 下），以便变更可审计、可追溯。

#### Scenario: 默认消息来源可追溯
- **WHEN** 开发者需要修改默认连通性短文本
- **THEN** 其 SHALL 通过修改上述资源文件完成
- **AND** 其 SHALL NOT 仅在业务代码中硬编码默认字符串
