## Why

首页目前仅为文字占位，无法支撑产品主流程的视觉与信息结构验证。需要在 Flutter 客户端搭出与设计稿一致的首页**整体架子**（可滚动、分区清晰、数据先 mock），并为后续接相机路由与后端数据预留扩展点，同时与底部「记录」Tab 建立明确的跳转契约。

## What Changes

- 在「首页」Tab 内实现纵向滚动的首页布局：顶部日期与固定标语、拍照主入口、`CalorieProgressCard` 展示今日摄入与进度、最近记录模块（含「查看全部」）、AI 建议区。
- 复用现有 `lib/core/widgets` 中的业务/基础组件（`CaptureCard`、`CalorieProgressCard`、`MealRecordCard`、`AIInsightCard`、`SectionTitle`、`ShijiCard` 等），**不强制**首版与 UI 稿像素级一致；细节迭代后续单独变更。
- 今日热量相关数据、最近一条记录、AI 文案均使用 **mock**；拍照点击预留 `onTap`/路由回调接口，首版可不进入真实相机页。
- 「查看全部」**切换到底部「记录」Tab**（`StatefulNavigationShell.goBranch`），不进入记录子路由。
- **进度条约定（选项 A）**：当已摄入热量 **大于或等于** 日目标时，进度条填充比例仍 **clamp 至 100%**（满条），但进度条 **指示色使用红色**（danger），表示已达标或超出；未超目标时保持现有主色（token）进度条样式。

## Capabilities

### New Capabilities

- `mobile-home-ui`：首页 Tab 的布局结构、各区块职责、mock 数据边界、进度条超额红色规则、与记录 Tab 的切换行为、拍照入口预留扩展点。

### Modified Capabilities

- `meal-app-shell`：原「各 Tab 均为文字占位」的要求需更新为：**首页**展示上述首页架子，**其余 Tab** 仍保持占位或可后续迭代；规范层面明确首页不再仅为「首页占位」单句文案。

## Impact

- **代码**：`apps/mobile` 下首页 Tab 对应页面（当前多为 `HomeTabPlaceholder` 或路由分支中的占位）需替换为首页页面组件；可能新增 `features/home/` 下页面与轻量组装 widget；`CalorieProgressCard` 或调用方需支持「超额红色」着色逻辑（若组件本身不承载该逻辑，则由页面根据 `consumed`/`goal` 传参）。
- **路由**：沿用现有 `go_router` + `StatefulNavigationShell`，从首页触发「记录」分支切换；不新增记录栈子路径。
- **测试**：Widget 测试需覆盖首页关键文案/区块存在性及「查看全部」触发分支切换（与现有 `main_shell_page_test` 风格对齐）。
- **依赖**：不新增必需后端或第三方 SDK；相机能力延后接入。
