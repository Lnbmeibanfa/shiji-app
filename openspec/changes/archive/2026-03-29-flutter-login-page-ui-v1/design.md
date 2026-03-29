## Context

- 登录基础设施已完成：`AuthRepository`（发码、短信登录）、`AuthController`、`GoRouter` 门禁、`ShijiButton`、`AppSnackbar` 等见 `flutter-client-infrastructure-v1`。
- 当前 `LoginPlaceholderPage` 仅为占位文案，无表单与交互。
- 产品设计稿（欢迎区、双输入框、获取验证码、协议勾选、主按钮）与食迹 Design Token（低饱和绿、浅底、大圆角）一致；实现 MUST 使用 `AppColors` / `AppRadius` / `AppTypography` / `AppSpacing` 等，不得在业务组件内随意写死 hex。
- 后端联调约定：`agreements` 与 `services/api/docs/auth-api.md` 一致，示例使用 `USER_AGREEMENT`、`PRIVACY_POLICY` 与 `agreementVersion: "v1"`；开发环境验证码见后端日志 `SMS stub sent`。

## Goals / Non-Goals

**Goals:**

- 交付与设计图信息架构一致的登录页：品牌区、手机号、验证码 + 获取验证码、协议行、主按钮登录。
- 发码、登录走 `AuthRepository`，成功写入 `AuthController.setSessionToken`，错误用统一反馈展示。
- 获取验证码按钮支持冷却（至少 60s 本地倒计时，与后端 1 分钟频控语义对齐；若后端返回频控错误，展示 `message` 并可选重置倒计时策略见下）。
- 未勾选协议时禁止登录提交（前端校验 + 后端仍可能返回 10007）。
- 协议链接可点击：MVP 可用占位（SnackBar「待接入」）或打开占位路由；后续再接独立 Web 页或 `url_launcher`。

**Non-Goals:**

- 不修改后端 API 或 OpenAPI 定义。
- 不在本变更引入 `intl` 全文案抽离（若仅登录页中文，可硬编码字符串；后续 i18n 单独变更）。
- 不实现完整《用户协议》/《隐私政策》正文页（可后续单独变更）。

## Decisions

1. **页面形态**：使用 `Scaffold` + `SafeArea` + 纵向 `SingleChildScrollView`，避免小屏键盘遮挡；品牌区在上、表单在中、主按钮在下，水平 padding 使用 `AppSpacing` 页面边距 Token。
2. **状态管理**：使用 `ConsumerWidget` / `ConsumerStatefulWidget` + Riverpod（`ref.read` Repository、`AuthController`），与 `providers.dart` 一致；本地 UI 状态（手机号、验证码、勾选、倒计时）放在 `State` 或小型 `StateNotifier`，避免在 Widget 内直接 `dio`。
3. **品牌 logo**：使用 **`Image.asset`** 展示位图资源（如 `assets/images/logo.png`），外层为浅底圆角容器（`AppColors` + `AppRadius`），**不得**用 `Icons.*` 等 Material 图标拼作正式 logo。占位阶段可在 `pubspec.yaml` 注册同名资源并放入临时 PNG，产品图到位后**仅替换资源文件**即可，无需改布局逻辑。
4. **输入框**：复用 `InputDecoration` + `filled: true`、`fillColor: AppColors.bgCard` 或 `bgMuted`，圆角 `AppRadius.sm`（12）与设计图「约 12」一致；标签可用 `Text` + `bodySmall` 或 `labelMedium` 置于上方。
5. **获取验证码**：右侧 `TextButton` 或浅色 `OutlinedButton` 样式，文案色 `AppColors.primary` 或 `tagGreenText`；禁用态在倒计时或请求中 `isLoading`。
6. **协议**：`Row` + `Checkbox` + `RichText`/`Text.rich` 与 `TapGestureRecognizer` 区分链接区域；链接色 `AppColors.primary`（或 `tagGreenText`）。勾选为 false 时主按钮 `onPressed: null` 或 SnackBar 提示。
7. **协议版本常量**：与文档 curl 一致，集中在一处（如 `features/auth/constants/agreement_constants.dart` 或 `AgreementAcceptance` 工厂），值为 `USER_AGREEMENT`/`PRIVACY_POLICY` + `v1`，便于后续仅改一处。
8. **Web 联调**：若遇 CORS，由后端或本地代理解决（本变更可在 `README` 片段注明）；不阻塞 UI 实现。

**Alternatives considered**

- **全表单用 `flutter_form_builder`**：依赖与学习成本增加，MVP 用 `TextFormField` + 简单校验即可。
- **倒计时与后端频控强绑定**：需后端返回 `retryAfter` 字段；当前 API 未提供，故以本地 60s + 错误 message 为主。

## Risks / Trade-offs

- **[Risk] 设计稿色值与 Token 微差** → **Mitigation**：以 `openspec/specs/design-tokens` 为准，视觉验收以 Token 语义一致为目标，不强制逐像素取色。
- **[Risk] Web 存储与安全** → **Mitigation**：token 仍经 `AuthStorage`；Web 下 `flutter_secure_storage` 行为依插件文档，已知问题在基础设施变更已讨论。
- **[Risk] 协议链接占位** → **Mitigation**：产品可后续替换为真实 URL；spec 要求链接可点且行为可测。

## Migration Plan

- 替换 `LoginPlaceholderPage` 为真实页面类（可保留文件名或重命名为 `login_page.dart`），`GoRouter` 路由 path 不变。
- 无数据迁移；无服务端变更。

## Open Questions

- 协议正文是否必须在 MVP 内可访问：当前默认链接占位或 SnackBar，产品确认后可改为 `url_launcher` 打开静态页。
- 获取验证码冷却是否在 429/业务码 10001 时延长倒计时：实现时可优先展示后端 `message`，本地倒计时仍按 60s 起算。
