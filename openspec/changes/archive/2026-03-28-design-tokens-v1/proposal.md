## Why

食迹前端需要一套**冻结的 Design Token**，以便 Flutter 实现、AI 生成界面与 Figma 手工稿对齐；否则易出现随意 hex、间距与圆角不一致，破坏「轻疗愈、不评判」的产品气质。现需将《食迹 Design Token 规范 v1》纳入 OpenSpec，作为唯一事实来源。

## What Changes

- 新增 **Design Token v1** 规范：颜色、字体层级、圆角、间距、阴影、组件尺寸、图标及关键组件（ShijiButton、AIInsightCard 等）的视觉定稿。
- 明确 **禁止**：页面/组件内直接写死 hex（须仅用 Token）；使用鲜红等高刺激色表达风险。
- 与 `docs/` 或 `apps/mobile` 内设计文档对齐时，以本变更产出的 **spec** 为准；后续实现任务将落地 `AppColors` / `AppSpacing` / `AppRadius` 等 Flutter 文件。

## Capabilities

### New Capabilities

- `design-tokens`：食迹移动端 Design Token v1（品牌基调、色板、排版、圆角、间距、阴影、尺寸、图标、组件视觉规则、Flutter 命名约定、AI 生成约束与首批组件清单）。

### Modified Capabilities

- `client-architecture`：补充「界面实现须遵守 `design-tokens` 规范，颜色/间距/圆角不得脱离 Token」的约束说明（与现有 feature-first、不经客户端直连 AI 等规则并列）。

## Impact

- **Flutter**：`apps/mobile` 将新增或调整 token 类与主题；新组件须引用 Token。
- **AI / Cursor**：生成 Flutter 页面与组件时必须遵守 spec 中的 Token 与固定提示词约束。
- **设计**：Figma 与手工稿须与 Token 表一致。
- **文档**：可与 `apps/mobile/UI设计规范.md` 交叉引用，规范冲突时以 `openspec/specs/design-tokens` 为准。
