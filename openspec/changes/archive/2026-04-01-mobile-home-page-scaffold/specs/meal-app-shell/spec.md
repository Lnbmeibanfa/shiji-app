# 主壳与底部导航（meal-app-shell）— 增量

本文件仅描述相对 `openspec/specs/meal-app-shell/spec.md` 的变更；归档时合并。

---

## MODIFIED Requirements

### Requirement: 各 Tab 主内容区 MUST 使用文字占位

在实现完整业务页面前，**记录、复盘、我的**三个 Tab 对应的主内容区 MUST 各自展示可读占位文案，且 MUST 能区分当前为哪一 Tab（例如展示 Tab 名称或「{Tab 名}占位」）。**首页** Tab 主内容区 MUST 展示符合 `mobile-home-ui` 规范的首页 UI 架子（可滚动、分区、mock 数据），MUST NOT 仅为单句「首页占位」类文案。上述首页与占位页 MUST NOT 依赖网络或后端接口。

#### Scenario: 首页 Tab 展示首页架子

- **WHEN** 用户选中「首页」Tab
- **THEN** 主内容区展示符合 `mobile-home-ui` 的首页结构（问候、拍照入口、热量卡片、最近记录、AI 建议），而非仅单行占位文字

#### Scenario: 记录 Tab 占位

- **WHEN** 用户选中「记录」Tab
- **THEN** 主内容区显示明确标识「记录」的占位文字

#### Scenario: 复盘 Tab 占位

- **WHEN** 用户选中「复盘」Tab
- **THEN** 主内容区显示明确标识「复盘」的占位文字

#### Scenario: 我的 Tab 占位

- **WHEN** 用户选中「我的」Tab
- **THEN** 主内容区显示明确标识「我的」的占位文字
