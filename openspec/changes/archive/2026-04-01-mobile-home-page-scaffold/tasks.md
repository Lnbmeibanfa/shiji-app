## 1. 基础设施与修复

- [x] 1.1 修复 `SectionTitle` 中 `trailing` 在 `Row` 的合法接入（`if` / collection `if` 等），确保「查看全部」可挂载且不破坏分析
- [x] 1.2 在 `AppColors`（或现有 token）中确认用于进度条超额的红/danger 色；若无则新增语义色并在 `CalorieProgressCard` 或调用方使用

## 2. 首页页面与布局

- [x] 2.1 新增 `features/home/` 下 `HomePage`（或同名），使用 `SingleChildScrollView`/`CustomScrollView` + `SafeArea` 与 token 间距组装五大区块
- [x] 2.2 实现问候区：固定中文日期格式（年/月/日/星期）+ 前端固定一句标语，样式用 `AppTypography` / `AppColors`
- [x] 2.3 嵌入 `CaptureCard`，`onTap` 留空回调或 TODO 命名方法，便于后续接路由

## 3. 热量卡片与进度条（约定 A）

- [x] 3.1 用 mock 数据展示已摄入、还可吃、目标；进度比例 `min(consumed/goal, 1.0)`；当 `consumed >= goal` 时进度条指示色为红色，否则为主色（在组件参数或页面层实现，取最小改动）
- [x] 3.2 若现有 `CalorieProgressCard` API 不足以表达双列数字，在页面内用 `ShijiCard`+`Row` 组合或小幅扩展组件参数

## 4. 最近记录与 Tab 切换

- [x] 4.1 `SectionTitle` 标题「最近记录」+ `trailing`「查看全部」；点击时 `StatefulNavigationShell.of(context).goBranch(1)`（或等价注入方式）
- [x] 4.2 使用 `MealRecordCard` 展示一条 mock 记录（标题/副标题按现有 API 填入餐别时间、kcal、描述）

## 5. AI 建议与路由接入

- [x] 5.1 嵌入 `AIInsightCard`（或等价），使用 mock 文案满足设计方向
- [x] 5.2 将 `go_router` 中首页 Tab 分支由 `HomeTabPlaceholder` 替换为 `HomePage`（或包装 `ProviderScope` 如需）

## 6. 测试与自检

- [x] 6.1 更新或新增 widget 测试：首页关键区块存在；点击「查看全部」后 `goBranch` 被调用或 Tab 索引为记录（与现有 `main_shell_page_test` 风格一致）
- [x] 6.2 `flutter analyze` / 现有测试套件通过
