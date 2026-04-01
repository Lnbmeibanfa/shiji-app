## Context

Flutter 移动端已具备 `go_router` 四 Tab 主壳、`core/widgets` 下与设计规范对齐的一批组件（含 `CaptureCard`、`CalorieProgressCard`、`MealRecordCard`、`AIInsightCard`、`SectionTitle`），以及 `AppTypography` / `AppColors` 等 token。首页 Tab 仍为 `HomeTabPlaceholder`。产品已确认首版以**现有组件形态**拼装架子，视觉差异后续再收。

## Goals / Non-Goals

**Goals:**

- 首页为可滚动单列布局，模块顺序：问候区（日期固定格式「年月日 + 星期」+ 前端固定一句标语）→ 拍照入口 → 今日摄入卡片（mock：`consumed`、`remaining`、`goal`）→ 最近记录（`SectionTitle` + 单条 `MealRecordCard`）→ AI 提示（`AIInsightCard` 或等价组合）。
- 「查看全部」切换到「记录」Tab（`navigationShell.goBranch(1)`），不推子路由。
- 「拍一顿」点击预留回调/路由口子，首版无真实相机实现亦可。
- 进度条按**约定 A**：`consumed >= goal` 时 `value` 仍 clamp 为 `1.0`，**进度条颜色**改为红色（使用 `AppColors` 中已有 danger/错误色或新增 token，优先复用 token）。
- 修复或规避 `SectionTitle` 中 `trailing` 接入 `Row` 的合法写法（若当前源码无效），以便「查看全部」可挂载。

**Non-Goals:**

- 对接后端 API、真实 AI 文案接口、相机/`image_picker` 链路。
- 记录/复盘/我的真实页面。
- 与 UI 稿像素级对齐（用户明确后续再调）。

## Decisions

| 决策 | 选择 | 说明 |
|------|------|------|
| 组件策略 | 优先复用 `lib/core/widgets` 现有实现 | 降低首版成本；布局与 API 不满足处用页面层组装或最小扩展。 |
| 首页归属 | `features/home/` 下单页 + 可选 `widgets/` | 与 `shell` 解耦，Tab 仅引用 `HomePage`。 |
| 记录 Tab 切换 | `goBranch(1)` | 与 `MainShellPage` 中 Tab 顺序一致（0 首页 1 记录…）；需在 `HomePage` 取得 `StatefulNavigationShell`（如 `StatefulNavigationShell.of(context)`）或经父级传入回调。 |
| 进度条 A | clamp + 变色 | 满条不溢出屏幕，红色传达「已达/超目标」；若产品后续要「条外示意超额」再单独立项。 |
| 日期文案 | `intl` 或手动拼接 | 固定中文「M月d日 EEEE」类展示即可，与系统 locale 解耦首版可接受。 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| `CaptureCard` 为横排布局，与稿竖排不一致 | 首版不改组件，产品已知悉；后续可加重构或 `layout` 参数。 |
| `CalorieProgressCard` API 较简，双列数字需在页面拼 | 页面层用 `Row` + 两个 `Text` 包在 `ShijiCard` 内，或小幅扩展组件参数；以最小 diff 为准。 |
| `MealRecordCard` 仅两行文案 | 首版将餐别+时间、kcal、描述合并/拆入 `title`/`subtitle` 或扩展可选第三行（实现阶段再选更小改动路径）。 |
| 测试依赖 Shell 上下文 | 与现有测试一致，使用 `MaterialApp` + 带 `StatefulNavigationShell` 的测试桩或 mock `goBranch`。 |

## Migration Plan

- 单客户端发版路径：无数据迁移；合并后首页用户可见新 UI。
- 回滚：恢复首页 Tab 为占位 Widget 即可。

## Open Questions

- 红色进度条是否需在 **仅达到目标未超过**（`consumed == goal`）与 **超过**（`consumed > goal`）之间区分深浅或文案提示：当前与产品约定统一红色即可，若需细分可在实现 PR 中再确认。
