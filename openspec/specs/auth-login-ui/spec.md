# 登录页 UI（auth-login-ui）

本规范定义食迹 Flutter 客户端**短信验证码登录页**的界面与交互需求，与 `services/api/docs/auth-api.md` 及 `AuthRepository` 行为一致。

---

## 规范要求

### Requirement: 登录页呈现与设计稿一致的信息架构

系统 SHALL 在登录路由展示完整登录页，包含：顶部品牌区（应用图标与欢迎标题、副标题）、手机号输入区、验证码输入区与「获取验证码」操作、协议勾选行（含《用户协议》《隐私政策》可区分点击区域）、底部主操作「登录」按钮。页面背景 SHALL 使用 Design Token 中的浅色背景（如 `bgPrimary`），主按钮 SHALL 使用主色 Token 与 `ShijiButton` 或等价视觉（白字、圆角、全宽）。

#### Scenario: 用户打开登录页

- **WHEN** 用户未登录且导航至登录路由
- **THEN** 系统展示上述区域且文案包含「欢迎来到食迹」及副标题「让我们一起开始健康饮食之旅」语义等价表述（允许标点微调）

### Requirement: 表单与校验

系统 SHALL 提供手机号与验证码文本输入；手机号 SHALL 符合国内 11 位以 1 开头的常见约束（与后端 `^1\d{10}$` 一致）。验证码 SHALL 限制为数字输入。用户未勾选协议前，系统 MUST NOT 执行登录提交（禁用主按钮或提示并拒绝提交）。

#### Scenario: 未勾选协议点击登录

- **WHEN** 用户未勾选协议且触发登录
- **THEN** 系统不调用登录接口并提示用户先阅读并同意协议

#### Scenario: 手机号格式无效

- **WHEN** 用户输入不符合规则的手机号并尝试获取验证码或登录
- **THEN** 系统在前端拒绝并提示用户修正（不发送无效请求）

### Requirement: 获取验证码与冷却

系统 SHALL 提供「获取验证码」操作，调用 `AuthRepository.sendSmsCode`。成功发送后 SHALL 进入不少于 60 秒的本地倒计时，期间按钮处于不可重复点击状态。若接口返回业务错误（如发送过于频繁），系统 SHALL 展示后端返回的 `message`（或统一解析后的错误文案）。

#### Scenario: 获取验证码成功

- **WHEN** 用户输入合法手机号且点击「获取验证码」且接口成功
- **THEN** 系统开始倒计时并可在开发环境依后端说明从日志获取验证码

### Requirement: 登录与协议体

用户勾选协议后，系统 SHALL 使用与勾选状态一致的 `agreements` 列表调用 `AuthRepository.loginWithSmsCode`，其中 `agreementType` 与 `agreementVersion` MUST 与产品/后端约定一致（当前为 `USER_AGREEMENT`、`PRIVACY_POLICY` 与 `v1`，与 `auth-api.md` 示例一致），且 `accepted` 为 true 仅当用户已勾选。登录成功后 SHALL 调用 `AuthController.setSessionToken` 以触发路由进入已登录区域。

#### Scenario: 登录成功

- **WHEN** 用户输入正确验证码、已勾选协议且后端返回成功
- **THEN** 系统持久化会话并导航至已登录首页（或主 shell）路径

### Requirement: 视觉必须使用 Design Token

登录页实现 MUST 通过 `AppColors`、`AppRadius`、`AppTypography` 及间距 Token（如 `AppSpacing`）引用颜色、圆角、字号与间距，MUST NOT 在业务 Widget 的 `build` 方法中直接书写 `Color(0xFF......)` 作为样式（`AppColors` 定义文件除外）。

#### Scenario: 代码审查

- **WHEN** 审查者检查 `features/auth` 下登录相关 Widget 源码
- **THEN** 不应出现未收敛的十六进制色值用于样式

### Requirement: 网络访问不得绕过 Repository

登录页及附属 Widget MUST NOT `import` 或使用 `dio` 直接请求；发码与登录 MUST 仅通过 `AuthRepository`（或项目统一之认证门面）完成。

#### Scenario: 发起登录请求

- **WHEN** 用户提交登录
- **THEN** 仅由 `AuthRepository.loginWithSmsCode` 发起对应 HTTP 调用

### Requirement: 协议链接可点击

《用户协议》与《隐私政策》在文案上 SHALL 为可独立识别的点击区域（如 `Text.rich` 与手势识别）。MVP 允许链接跳转至占位行为（例如提示待接入或应用内占位页），但 SHALL 为后续替换为真实 URL 或 WebView 预留清晰扩展点。

#### Scenario: 用户点击协议链接

- **WHEN** 用户点击《用户协议》或《隐私政策》链接
- **THEN** 系统执行已实现的占位或导航行为且不导致应用崩溃
