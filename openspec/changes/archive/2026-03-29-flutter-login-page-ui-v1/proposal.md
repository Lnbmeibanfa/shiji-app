## Why

当前登录为占位页，无法完成短信验证码登录的完整体验与协议前置校验。MVP 需要按产品设计实现登录界面，并与既有 `AuthRepository`、`AuthController`、后端 `/api/auth` 契约对齐，使用户可在 Web（及后续 Android）上走通发码、勾选协议、登录与会话写入。

## What Changes

- 将 `features/auth` 下登录占位页替换为**可交互登录页**：品牌区（图标/欢迎文案）、手机号与验证码输入、获取验证码按钮（含冷却/频控错误提示）、协议勾选与《用户协议》《隐私政策》链接样式。
- 登录提交使用 `ShijiButton`（loading / 防重复点击），成功路径调用 `loginWithSmsCode` 后 `setSessionToken`，错误通过统一反馈（SnackBar 等）展示后端 `message` / 业务码。
- 视觉遵循 **Design Token**（`AppColors`、`AppRadius`、`AppTypography`、`AppSpacing` 等），与设计图气质一致（浅底、低饱和绿、大圆角、清晰层级），**禁止**在业务 Widget 内随意硬编码色值。
- 协议版本与类型与后端 `AgreementAcceptanceDto` 一致；勾选状态与请求体一致，**禁止**硬编码为已同意。

## Capabilities

### New Capabilities

- `auth-login-ui`：定义登录页信息架构、表单与协议交互、与 Repository/AuthController 的衔接及无障碍与错误呈现等需求。

### Modified Capabilities

- （无）本变更为在既有客户端架构与认证 API 契约上实现 UI，不修改 `openspec/specs/` 中已有能力的需求条文；合规性通过 `auth-login-ui` 新增需求表述。

## Impact

- **代码**：`apps/mobile/lib/features/auth/pages/`（替换占位页，可拆分为局部 Widget）、可能新增 `features/auth/widgets/`；复用 `core/widgets/shiji_button.dart`、`core/feedback/`、`providers.dart` 已有依赖。
- **资源**：设计参考图可置于仓库 `assets/` 或文档引用路径，供实现与验收对照。
- **API**：无后端契约变更；沿用 `services/api/docs/auth-api.md` 与 `AuthRepository` 已有路径。
- **依赖**：无新增 package 预期（Material 3 + 现有主题即可）。
