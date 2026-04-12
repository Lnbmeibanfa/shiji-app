# 首页 UI（mobile-home-ui）— 本变更增量

本文件仅包含本变更对既有 `openspec/specs/mobile-home-ui/spec.md` 的 **MODIFIED** 条文；归档时合并回主规范。

---

## MODIFIED Requirements

### Requirement: 拍照主入口 MUST 可点击并导航至记录饮食

拍照主入口 MUST 使用现有 `CaptureCard`（或等价封装）呈现，且 MUST 响应点击。用户点击后 MUST 通过声明式路由（如 `go_router`）导航至**记录饮食**全屏页面（路径常量如 `RoutePaths.recordMeal`，与 `record-meal-ui` 变更一致），MUST NOT 在首版强制依赖除已选用插件外的额外权限。该全屏页 MUST 承载选图/拍照上传与记录表单（餐别、占位区块、手动添加食物、情绪、备注、保存）。

#### Scenario: 用户点击拍照入口进入记录饮食

- **WHEN** 用户点击拍照主入口
- **THEN** 应用导航至记录饮食全屏路由，界面不崩溃
