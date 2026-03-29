## Why

当前后端仅有项目骨架，尚未具备可用的登录能力。为了支持移动端后续功能接入，需要尽快落地基于短信验证码的登录、会话恢复与退出流程，并满足协议同意留痕与基础安全控制要求。

## What Changes

- 新增短信验证码登录能力：发送验证码、验证码校验登录、登录态签发。
- 新增会话管理能力：自动恢复会话、单设备策略下的会话互斥、全部退出登录。
- 新增协议同意登录前置约束：未同意用户协议与隐私政策时禁止登录，并记录同意日志。
- 明确验证码与会话关键策略：验证码有效期 1 分钟、验证码错误最大次数 10 次、会话有效期 30 天。
- 统一登录相关接口返回结构与错误码语义，便于客户端稳定接入。

## Capabilities

### New Capabilities
- `user-auth`: 用户认证与会话管理能力，覆盖短信验证码登录、会话恢复、退出登录、协议同意前置与留痕。

### Modified Capabilities
- `server-architecture`: 增加认证模块分层与边界约束，明确登录能力按 Controller/Service/Repository 分层实现并遵循统一返回结构。

## Impact

- 影响后端代码目录：`services/api/src/main/java/com/shiji/api/modules/user`（或新增 `modules/auth`）及 `common`、`config`。
- 新增或更新接口：发送验证码、验证码登录、恢复会话、退出登录。
- 影响数据访问：`user`、`user_auth`、`user_session`、`sms_code_log`、`user_agreement_accept_log`。
- 影响中间件与依赖：Spring Security 鉴权链、Redis（频控/会话辅助）、短信服务适配层（先预留接口，供应商实现后接入）。
