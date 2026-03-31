## Why

高保真设计已定义「食记」类应用的主结构：底部四个入口（首页、记录、复盘、我的）与可切换的主内容区。当前移动端仅有占位首页与单一路由，缺少主壳层与 Tab 切换，无法按设计迭代各 Tab 内容。需要先落地导航骨架与占位页，再逐步替换为真实业务 UI。

## What Changes

- 引入**底部导航主壳**（`BottomNavigationBar` 或 `NavigationBar` 等 Material 3 组件），固定四个 Tab，点击切换对应主内容区。
- 四个 Tab 对应页面**先用纯文字占位**（与设计稿中的区块文案解耦，仅标识 Tab 名称），后续再接入首页卡片、记录列表等。
- 扩展 `go_router` 路由：在已登录主路径下支持壳层与子路径（或等价状态切换），保证深链与返回栈行为可演进。
- 可选：沿用设计体系中的主色/圆角（`app_colors`、`shiji_theme`），壳层样式与现有主题对齐，**不**在本变更中实现完整首页卡片与数据。

## Capabilities

### New Capabilities

- `meal-app-shell`：定义底部四 Tab 的标签与图标语义、选中态、点击切换主内容、各 Tab 占位内容的最小展示约定，以及与 `go_router` 集成的路由/重定向边界。

### Modified Capabilities

- （无）已登录仍进入 `/home`（或等价主路径）即可；行为细化由 `meal-app-shell` 承载，不修改 `flutter-client-bootstrap` 中 token/门禁等既有要求。

## Impact

- **代码**：`apps/mobile/lib/core/routing/`（`app_router.dart`、`route_paths.dart`）、新增壳层与 Tab 占位页面（建议 `lib/features/shell/` 或 `lib/features/home/` 下子模块）。
- **依赖**：沿用现有 `go_router`；若使用 `StatefulShellRoute` 无需新 pub 依赖。
- **测试**：Widget 测试可覆盖 Tab 切换后可见文案；可与现有 `home_page_placeholder_test` 演进或替换。
