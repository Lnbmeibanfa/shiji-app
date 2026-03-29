# 客户端架构规范 — 增量（design-tokens-v1）

本文件为相对 `openspec/specs/client-architecture/spec.md` 的增量，归档时合并。

---

## ADDED Requirements

### Requirement: 界面实现 MUST 遵守 Design Token 规范

Flutter 界面实现 MUST 遵守 `openspec/specs/design-tokens`（本变更归档后路径）中的 Design Token v1：颜色、圆角、间距、阴影、排版层级与已定组件视觉规则 MUST 通过规范定义的 Token 或等价主题 API 引用，MUST NOT 在业务组件中随意硬编码十六进制色值或未文档化的尺寸。

#### Scenario: 新增页面与组件

- **WHEN** 开发者在 `features/` 下新增页面或通用组件
- **THEN** MUST 使用 Token 规范中的颜色与间距圆角（如 `AppColors` / `AppSpacing` / `AppRadius`），MUST NOT 仅因方便在 Widget 内新增 `Color(0xFF...)`

#### Scenario: AI 生成代码

- **WHEN** 使用 AI 生成 Flutter UI 代码
- **THEN** 生成结果 MUST 符合 `design-tokens` 规范中的 AI 约束章节（仅用 Token 常量、禁止组件内写死色值与随意圆角间距）
