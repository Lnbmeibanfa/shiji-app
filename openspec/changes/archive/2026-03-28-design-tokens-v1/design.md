## Context

食迹 Flutter 端将高频使用卡片、进度与 AI 建议等组件；若无冻结 Token，AI 与人工实现易产生不一致，并损害「轻疗愈、不高压」体验。本变更将《食迹 Design Token 规范 v1》写入 OpenSpec，并约定 Flutter 侧文件组织与落地顺序。

## Goals / Non-Goals

**Goals:**

- 将颜色、排版、圆角、间距、阴影、关键尺寸与 9 个首批组件的视觉规则定为**唯一设计事实来源**。
- 约束实现层：**禁止**在业务 Widget 中硬编码 hex/随意 magic number（须通过 `AppColors` / `AppSpacing` / `AppRadius` 等）。
- 为 AI 生成代码提供可复制的固定提示词与组件优先级列表。

**Non-Goals:**

- 不在本变更中完成全部 Flutter 组件编码实现（见 `tasks.md`）。
- 不规定具体第三方图标库选型（仅规定尺寸与线性、简洁风格）。
- 不替代 Figma 文件结构，仅约束 Token 数值与命名与 spec 一致。

## Decisions

1. **规范存放位置**  
   - 归档后：`openspec/specs/design-tokens/spec.md` 承载完整 Token 与组件规则。  
   - 本 change 内 `specs/design-tokens/spec.md` 为待归档增量。

2. **Flutter 文件建议**  
   - `app_colors.dart`、`app_spacing.dart`、`app_radius.dart`（及后续 `app_typography.dart` / `app_shadows.dart` 若拆分）放在 `apps/mobile/lib/core/` 或 `apps/mobile/lib/app/theme/`，与 feature 解耦；具体路径在实现任务中敲定。

3. **与 `client-architecture` 的关系**  
   - 在客户端架构规范中 **ADDED** 一条：界面实现 MUST 遵守 `design-tokens`，避免与 feature-first 并列冲突。

4. **禁用色**  
   - 风险与「超出」类反馈统一使用 `dangerSoft` / `warning` 等已定色，**禁止** `#FF0000` 及同类高刺激红。

5. **主题扩展**  
   - 第一版以静态 `const` Token 为主；若未来引入 `ThemeExtension`，须在 change 中更新 spec，不得静默新增未文档化 Token。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 历史或第三方代码硬编码颜色 | 代码评审与静态检查（后续可在 tasks 中加入 lint/自定义检查） |
| Token 表过长导致 spec 难读 | 保持表格化摘要 + 要求与 `UI设计规范.md` 互链；重大变更走新 Change |
| 与设计稿临时不一致 | 以 OpenSpec `design-tokens` 为准；改稿先改 spec 再改代码 |

## Migration Plan

1. 合并本 change 并归档后，在 Flutter 中新增 Token 文件。  
2. 新代码一律使用 Token；旧代码按模块逐步替换，不强制单次 PR 全量改完（可在 tasks 中拆阶段）。

## Open Questions

- 是否在 CI 中增加简单脚本扫描 `Color(0x` 在 `features/` 下的使用（排除 `app_colors.dart`）——待实现阶段决定。
