## Why

当前「记录饮食」仅落在 `CameraPage` 与 `/camera` 路径上，语义偏窄，无法承载设计稿中的餐别选择、手动食物、情绪与备注等完整表单。需要在不上线 AI、不接入「最近一餐」数据接口的前提下，先把**记录饮食全屏页**按设计落地，并拆分为可复用组件，为后续接保存接口与历史餐复用打基础。

## What Changes

- **路由与命名**：用「记录饮食」语义的全屏路由**替换** `/camera`（**BREAKING**：路径与入口引用变更），与 UI 文案一致。
- **页面结构**：实现与设计稿一致的纵向布局：照片区（沿用上传能力）、餐别单选、**「最近一餐」区块仅占位 UI**（不接 API）、手动添加食物入口、可选情绪、可选备注、底部保存。
- **业务规则（先定稿）**：无照片时提交体中的 `record_method` **MUST** 为 `manual`；**至少一条 `foodItem`** 才允许保存，否则主按钮**置灰不可点**。
- **实现顺序**：先按设计拆组件 → 实现各组件 → 再组装页面。

## Capabilities

### New Capabilities

- `record-meal-ui`：Flutter 端「记录饮食」全屏页面布局、组件拆分、交互状态（保存可用性、`record_method` 与照片关系）、与 `/camera` 路由迁移的 UI 侧约定。

### Modified Capabilities

- `mobile-home-ui`：首页「拍照主入口」点击行为从占位扩展为 **MUST** 导航至新的记录饮食全屏路由（取代仅预留扩展点的表述）。

## Impact

- **apps/mobile**：`RoutePaths`、`app_router`、首页与 Shell 占位中的 `push` 目标；`features/camera` 目录可重命名或保留并逐步迁移；新增/调整若干 Widget 文件。
- **后端**：本变更**不**新增 API；后续保存调用仍沿用既有 `POST /api/meal-records`（在实现任务中再接）。
