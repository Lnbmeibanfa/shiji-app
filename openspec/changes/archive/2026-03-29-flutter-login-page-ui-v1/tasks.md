# 任务清单：登录页 UI（flutter-login-page-ui-v1）

## 1. 契约与常量

- [x] 1.1 新增协议类型与版本常量（与 `auth-api.md` curl 示例一致：`USER_AGREEMENT`、`PRIVACY_POLICY`、`v1`），供登录请求组装 `agreements` 使用
- [x] 1.2 （可选）将设计参考图复制到 `apps/mobile/docs/` 或 `assets/` 并在 README 或本变更 `design.md` 指向，便于验收对照 — **跳过可选项**；品牌位图占位为 `assets/images/logo.png`（可替换），验收以 OpenSpec 与实现为准

## 2. 页面结构与样式

- [x] 2.1 实现登录页布局：`Scaffold` / `SafeArea` / `SingleChildScrollView`，品牌区（图标容器 + 主副标题），背景与间距使用 Design Token
- [x] 2.2 实现手机号、验证码输入区（标签在上、圆角填充输入框，禁止业务内硬编码 hex）
- [x] 2.3 实现「获取验证码」按钮：与输入框同一行或设计图布局；样式使用 Token；请求中禁用
- [x] 2.4 实现协议行：`Checkbox` + `Text.rich`，链接可点（MVP 可用 SnackBar 或占位导航）
- [x] 2.5 使用 `ShijiButton` 作为「登录」主按钮，支持 `isLoading`

## 3. 交互与业务接线

- [x] 3.1 实现手机号与验证码格式校验（手机号对齐后端 `^1\d{10}$`）
- [x] 3.2 实现获取验证码：调用 `AuthRepository.sendSmsCode`，错误用 `AppSnackbar`（或项目统一反馈）展示
- [x] 3.3 实现发送成功后本地 60s 倒计时，倒计时内禁止重复发送
- [x] 3.4 未勾选协议时禁止登录；勾选后调用 `loginWithSmsCode`，成功执行 `AuthController.setSessionToken`
- [x] 3.5 登录失败展示解析后的错误信息；登录中按钮 loading

## 4. 路由与收尾

- [x] 4.1 用新登录页替换 `LoginPlaceholderPage`（或重命名并更新 `app_router` import），路由 path 不变
- [x] 4.2 `flutter analyze` 与 `flutter test` 通过；必要时为校验逻辑或关键 Widget 补最小测试

## 5. 文档

- [x] 5.1 在 `apps/mobile/README.md` 增补一句：登录页已接 API，联调步骤仍指向 `auth-api.md` 与 `--dart-define`
