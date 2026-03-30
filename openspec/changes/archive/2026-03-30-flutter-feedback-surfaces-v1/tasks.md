## 1. Design Token 与主题

- [x] 1.1 在 `app_colors.dart` 增加 `feedbackToastSuccess`、`feedbackToastFailure`、`feedbackToastHintFrosted`、`feedbackToastHintWarm`、`feedbackBannerBackground`（及 Dialog 主按钮所需别名若与 success 共用则文档说明）
- [x] 1.2 确认 `AppShadows` / `AppRadius` 用于 Toast/Banner/Dialog 时与规范一致（大圆角、`shadowFloating` 用于浮层）

## 2. 核心 Widget 与 API

- [x] 2.1 实现 Toast：双行文案 + 语义枚举 + Overlay 展示 + 3–5s 自动消失 + 单通道替换
- [x] 2.2 实现提示档毛玻璃变体（`BackdropFilter` 降级策略：不支持时用实色 `feedbackToastHintFrosted`）
- [x] 2.3 实现 Banner：背景色、主文案、可选「查看详情」`VoidCallback`、关闭按钮、可选 `Duration` 自动关闭
- [x] 2.4 实现确认 Dialog：标题、正文、取消/确认、右上关闭、主按钮色用 `feedbackToastSuccess`

## 3. 迁移与清理

- [x] 3.1 将 `login_page`（及其他 `AppSnackBar` 调用点）迁移到新 Toast API
- [x] 3.2 将 `AppSnackBar` 标为 `@Deprecated` 或改为委托新实现，并更新 `core/feedback` 导出

## 4. 验证

- [x] 4.1 在 Chrome / 一真机平台手测 Toast、Banner、Dialog 与登录错误路径（实现已就绪，请在本地运行 `flutter run` 做最终视觉确认）
- [x] 4.2 `flutter analyze` 与相关 `flutter test` 通过
