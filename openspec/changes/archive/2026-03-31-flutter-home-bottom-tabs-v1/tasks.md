## 1. 路由与路径

- [x] 1.1 在 `route_paths.dart`（或等价）定义主壳及四 Tab 子路径常量，与 `go_router` branch 一致

- [x] 1.2 将 `app_router.dart` 中单一 `home` `GoRoute` 改为 `StatefulShellRoute`（或等价），四 branch 分别映射四个子路径

- [x] 1.3 校验 `redirect`：已登录进主壳默认 Tab；未登录访问主壳子路径仍跳转登录

## 2. 壳层与占位页

- [x] 2.1 新增主壳 Widget：`NavigationBar`/`BottomNavigationBar` + `StatefulShellBranch` 的 body（如 `Navigator` 子栈）

- [x] 2.2 为「首页」「记录」「复盘」「我的」各增一页级 Widget，主区域仅 `Text` 占位（文案可区分 Tab）

- [x] 2.3 移除或降级顶层对 `HomePagePlaceholder` 的依赖，避免双首页

## 3. 验证

- [x] 3.1 更新或新增 Widget 测试：切换 Tab 后可见对应占位文案

- [x] 3.2 在模拟器/真机手动走登录 → 主壳 → 逐 Tab 点击确认无异常
