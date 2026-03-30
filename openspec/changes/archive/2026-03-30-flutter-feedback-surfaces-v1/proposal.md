## Why

当前移动端仅有极简 `SnackBar` 封装与全屏 loading，缺少与产品视觉一致的分级反馈（成功 / 失败 / 轻提示、顶区公告、确认类弹窗）。设计侧已提供 Toast、Banner、Dialog 原型，需要在代码与规范中一次性收敛，避免各业务页面散落实现与硬编码色值。

## What Changes

- 在 `core/feedback/`（或等价路径）提供三类可复用表面：**Toast**（双行文案 + 分语义背景）、**Announcement Banner**（可关闭、可选自动消失、支持「查看详情」回调）、**确认 Dialog**（标题/正文/主次按钮，温和绿主色，无刺红）。
- 在 **Design Token** 中增加反馈专用语义色（成功、失败、提示两档、公告背景等），组件内禁止直接写 hex。
- 将现有 `AppSnackBar` 的调用逐步迁移到新 Toast API（可先并行保留别名，再在后续变更中删除旧 API）。
- Toast 默认 **3–5 秒**自动消失；Banner 支持 **手动关闭** 与 **可选定时关闭**。

## Capabilities

### New Capabilities

- `client-feedback`：定义 Flutter 端 Toast、Banner、Dialog 的行为、可访问性与与父组件的交互契约（回调、可选参数）。

### Modified Capabilities

- `design-tokens`：新增反馈表面专用颜色 Token（及与圆角、阴影、排版在反馈组件中的用法约束），与现有「禁止随意 hex」条款一致。
- `flutter-client-bootstrap`：将「全局错误提示经 SnackBar 封装」更新为经统一 **Toast/反馈 API**（或明确迁移路径），与会话/路由章节对齐。

## Impact

- **代码**：`apps/mobile/lib/core/feedback/`、`core/theme/app_colors.dart`、可能调整 `login_page` 等现有 `AppSnackBar` 调用点。
- **依赖**：无新业务后端依赖；纯 Flutter UI。
- **规范**：`openspec/specs/design-tokens`、`openspec/specs/flutter-client-bootstrap` 增量；新增 `openspec/specs/client-feedback`。
