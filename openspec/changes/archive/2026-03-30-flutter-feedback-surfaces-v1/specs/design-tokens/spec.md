# Design Token 规范（design-tokens）— 增量

本文件为相对 `openspec/specs/design-tokens/spec.md` 的增量，归档时合并。

---

## ADDED Requirements

### Requirement: 反馈表面专用颜色 Token

系统 MUST 在颜色 Token 表中增加 **反馈表面** 语义色，供 Toast、Banner、确认 Dialog 主按钮等使用，与现有 `success` / `warning` 等并存；数值 MUST 与下列十六进制一致（实现写在 `AppColors` 内，业务仅引用 Token 名）。

- `feedbackToastSuccess`：`#95AB99`（成功 Toast 与 Dialog 主确认按钮背景）
- `feedbackToastFailure`：`#E99B76`（失败 Toast，温和暖调，非刺红）
- `feedbackToastHintFrosted`：浅底 `#FAFAF9`，配合毛玻璃/半透明效果（具体 alpha 由实现与主题决定，但实色部分 MUST 对应该 Token）
- `feedbackToastHintWarm`：`#E9BC9C`（暖色实底提示）
- `feedbackBannerBackground`：`#FFF4E6`（公告 Banner 背景）

#### Scenario: Flutter 引用

- **WHEN** 开发者实现 `client-feedback` 规范中的 Toast、Banner 或 Dialog
- **THEN** 背景与按钮色 MUST 使用上述 Token 名之一，MUST NOT 在组件内写死与上表冲突的 hex

#### Scenario: 速查表一致

- **WHEN** 更新主规范中的颜色速查表
- **THEN** MUST 包含本要求中列出的反馈 Token 与 hex

---

## MODIFIED Requirements

### Requirement: 颜色 Token 为唯一色源

所有界面颜色 MUST 仅通过命名 Token 引用。Flutter 代码 MUST 通过 `AppColors`（或主题扩展中映射到相同数值的别名）访问颜色，MUST NOT 在组件 `build` 方法或样式中直接书写十六进制或 `Color(0xFF......)`（`AppColors` 定义文件内部除外）。

#### Scenario: 主色与按压态

- **WHEN** 实现主按钮、选中态、主拍照卡背景
- **THEN** MUST 使用 `primary`；按压态 MUST 使用 `primaryPressed`

#### Scenario: 背景与文本

- **WHEN** 实现页面、卡片、输入区、弱化区背景
- **THEN** MUST 从 `bgPrimary`、`bgCard`、`bgSecondary`、`bgMuted` 中选择，且文本 MUST 使用 `textPrimary` / `textSecondary` / `textTertiary` / `textInverse` 之一

#### Scenario: 边框与分割

- **WHEN** 使用边框或分割线
- **THEN** MUST 使用 `borderLight` 或 `divider`

#### Scenario: 功能色与标签色

- **WHEN** 表示成功、警告、AI 暖色块、轻风险或标签
- **THEN** MUST 仅使用规范已定：`success`、`warning`、`accentWarm`、`accentWarmInner`、`dangerSoft` 及 `tagGreen*`、`tagOrange*`、`tagYellow*`、`tagNeutral*`

#### Scenario: 反馈表面

- **WHEN** 实现 Toast、Banner 或 `client-feedback` 规范中的确认 Dialog
- **THEN** 背景与主操作色 MUST 使用 `feedbackToastSuccess`、`feedbackToastFailure`、`feedbackToastHintFrosted`、`feedbackToastHintWarm`、`feedbackBannerBackground` 等反馈 Token，MUST NOT 在组件内另写与定稿不一致的 hex
