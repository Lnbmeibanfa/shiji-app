## Context

移动端已有 `AppSnackBar`（单行 SnackBar）、`withAppLoadingOverlay` 与 `ShijiButton` 内联 loading，但缺少与 Figma 一致的分级反馈：双行 Toast（成功 / 失败 / 两类提示）、顶区可关闭 Banner（含「查看详情」）、以及温和风格的确认 Dialog。产品已定稿色值与交互原则（大圆角、轻阴影、避免刺红、Toast 3–5s、Banner 可手动或自动关闭）。

## Goals / Non-Goals

**Goals:**

- 在 `core/feedback/` 提供 **可复用 API**（静态方法或顶层函数 + Widget），覆盖 Toast、Banner、Dialog 三类表面。
- 颜色、圆角、阴影 **仅经 Design Token**；在 `AppColors`（及必要时 `AppShadows`）中增加 **反馈语义** 常量，与变更中 `design-tokens` 增量一致。
- Toast 支持 **主标题 + 副标题**、左侧语义图标；语义枚举：成功、失败、提示（浅底毛玻璃变体）、提示（暖色实底）。
- Banner：背景 `#fff4e6`，主文案 + 可选「查看详情」`VoidCallback` + 关闭；可选 `Duration` 自动移除。
- Dialog：白底、大圆角、`shadowFloating` 级阴影；标题、正文、右上关闭、取消 + 主操作（主按钮色与成功 Toast 绿一致）。

**Non-Goals:**

- 不替换或重做全屏 `withAppLoadingOverlay` 的视觉（仅可在文档中指向同一阴影语言）。
- 不实现推送通知、系统级通知栏。
- 不在本变更中 **强制** 删除 `AppSnackBar`（可先标记 `@Deprecated` 或保留别名指向新 Toast）。

## Decisions

1. **Toast 实现载体**  
   - **决策**：优先使用 **`Overlay` + `OverlayEntry`**（或 `Navigator` overlay）自定义 Toast 容器，以便实现 **毛玻璃**（`BackdropFilter` + 半透明 `#fafaf9`）与胶囊外形；成功/失败/暖色提示可用实色底。  
   - **备选**：纯 `SnackBar` 无法实现设计稿毛玻璃，且行为与边距控制较弱。  
   - **备选**：第三方 `fluttertoast` 包 — **不采用**，减少依赖与样式不可控。

2. **入口 API 形状**  
   - **决策**：`AppFeedback`（或 `ShijiToast` / `Feedback`）命名空间下：`showToastSuccess`、`showToastFailure`、`showToastHintFrosted`、`showToastHintWarm`（命名可在实现时微调成最短一致集）；`showBanner`、`showConfirmDialog`。  
   - 所有方法接收 `BuildContext`，内部处理 `mounted` 与安全 `Navigator`。

3. **Banner 挂载位置**  
   - **决策**：由调用方在 **页面 `Stack` 顶层** 插入，或通过 **根 `MaterialApp` 的 `builder` 包裹 `Stack`** 提供全局 Banner 层（若一次只显示一条，可用 `ValueNotifier` / `Inherited` 控制单例）。首版可采用 **函数式 API + Overlay** 与 Toast 一致，降低与路由耦合。

4. **Token 命名**  
   - **决策**：在 `AppColors` 增加例如 `feedbackToastSuccess`、`feedbackToastFailure`、`feedbackToastHintFrosted`、`feedbackToastHintWarm`、`feedbackBannerBackground`，数值与提案一致（成功 `#95AB99`、失败 `#E99B76`、提示浅 `#FAFAF9`、提示暖 `#E9BC9C`、Banner `#FFF4E6`）。  
   - **说明**：与现有 `success`/`warning` 数值不同是 **有意** 为反馈表面单独定稿，归档合并主 spec 时更新速查表。

5. **无障碍**  
   - Dialog 使用 `Semantics`；Toast 短时展示可 `excludeSemantics: false` 由系统读出文案（可选）。

## Risks / Trade-offs

- **[Risk]** 自定义 Overlay 与键盘、安全区、Web 表现差异 → **缓解**：在 `login_page` 与 `home` 各测一屏；Web 下限制 `BackdropFilter` 降级为纯色底。  
- **[Risk]** 与 `ScaffoldMessenger` 并存时重复遮挡 → **缓解**：新 Toast 关闭旧条目（单例队列）或文档约定「业务优先用一种」。  
- **[Trade-off]** Banner 全局单例 vs 每页自建 — 首版 **Overlay 单通道**，后续可抽 `FeedbackHost`。

## Migration Plan

1. 合并 Token 与组件实现。  
2. 将 `login_page`（及少数调用点）从 `AppSnackBar` 迁到新 API。  
3. 将 `AppSnackBar` 标为 deprecated 或改为委托新实现。  
4. 归档变更、合并主 `openspec/specs`。

## Open Questions

- 「提示」两档是否 **同时** 保留为两个公开 API，还是合并为一个 `variant` 枚举（建议枚举，减少方法爆炸）。
