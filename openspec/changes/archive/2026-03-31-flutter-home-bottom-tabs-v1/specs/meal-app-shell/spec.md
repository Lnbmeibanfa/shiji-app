# 主壳与底部导航（meal-app-shell）— 增量

本规范定义已登录用户主界面底部四 Tab 壳层与占位内容；归档后合并入 `openspec/specs/meal-app-shell/spec.md`（若主规范尚不存在则新建）。

---

## ADDED Requirements

### Requirement: 已登录主界面 MUST 展示底部四 Tab 导航

应用 MUST 在用户已登录且处于主界面壳层时，在屏幕底部展示固定导航栏，包含且仅包含四个条目，标签文字 MUST 依次为「首页」「记录」「复盘」「我的」。每个条目 MUST 配有图标（线框风格即可）。当前选中的 Tab MUST 在视觉上与非选中态区分。

#### Scenario: 进入主界面看到四 Tab

- **WHEN** 用户已登录且导航至主壳层默认入口
- **THEN** 界面底部显示四个 Tab，标签分别为「首页」「记录」「复盘」「我的」，且其中一项为选中态

#### Scenario: 点击 Tab 切换选中态

- **WHEN** 用户点击某一未选中的 Tab
- **THEN** 该 Tab 变为选中态，主内容区切换为对应 Tab 的页面

### Requirement: 各 Tab 主内容区 MUST 使用文字占位

在实现真实业务页面前，四个 Tab 对应的主内容区 MUST 各自展示可读占位文案，且 MUST 能区分当前为哪一 Tab（例如展示 Tab 名称或「{Tab 名}占位」）。占位页 MUST NOT 依赖网络或后端接口。

#### Scenario: 首页 Tab 占位

- **WHEN** 用户选中「首页」Tab
- **THEN** 主内容区显示明确标识「首页」的占位文字

#### Scenario: 记录 Tab 占位

- **WHEN** 用户选中「记录」Tab
- **THEN** 主内容区显示明确标识「记录」的占位文字

#### Scenario: 复盘 Tab 占位

- **WHEN** 用户选中「复盘」Tab
- **THEN** 主内容区显示明确标识「复盘」的占位文字

#### Scenario: 我的 Tab 占位

- **WHEN** 用户选中「我的」Tab
- **THEN** 主内容区显示明确标识「我的」的占位文字

### Requirement: 主壳层 MUST 与声明式路由集成

底部导航的 Tab 切换 MUST 通过声明式路由（如 `go_router`）驱动，使得每个 Tab 对应可寻址的子路径（或等价 branch），以便后续支持深链与测试。未登录用户 MUST NOT 通过正常门禁进入该壳层。

#### Scenario: 未登录无法进入主壳

- **WHEN** 用户未登录且尝试访问需认证的主壳路径
- **THEN** 应用按既有认证门禁处理（如跳转登录），不展示主壳内容
