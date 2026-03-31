## Context

- 移动端工程位于 `apps/mobile`，已使用 `go_router` 与 `AuthController` 做登录门禁；已登录默认进入 `RoutePaths.home`，当前 builder 为 `HomePagePlaceholder`。
- 设计稿要求底部四 Tab（首页 / 记录 / 复盘 / 我的）与可切换主内容区；本阶段仅需**文字占位**，与 `lib/core/widgets/` 中已有卡片组件解耦，后续再接高保真首页。

## Goals / Non-Goals

**Goals:**

- 已登录用户进入主路径时看到**带底部导航的壳层**，四个 Tab 可切换，各自主区域展示**明确占位文案**（如 Tab 名称或「{名称}占位」）。
- 路由层支持 Tab 与 URL 对应（推荐 `StatefulShellRoute` + branch paths），便于后续深链与测试。
- 视觉与 `shiji_theme` / `NavigationBar` 或 `BottomNavigationBar` 一致，选中态清晰。

**Non-Goals:**

- 不实现首页「拍一顿」、热量进度、最近记录等真实 UI 与数据。
- 不改变登录、Splash、token 存储与全局反馈规范行为。
- 不要求 Tab 状态跨进程持久化（可选后续）。

## Decisions

1. **路由结构：StatefulShellRoute**  
   - **理由**：四 Tab 为平级主导航，与 `go_router` 官方推荐一致，子路径如 `/home`、`/home/record` 等可独立深链。  
   - **备选**：单 Route + `IndexedStack` + 仅 query 区分 Tab——深链与浏览器/工具链一致性较弱，故不采用。

2. **占位实现**  
   - 每 Tab 一个轻量 `StatelessWidget`（或单文件内 private widget），中心或顶部 `Text` 即可，便于测试断言。

3. **与现有 `HomePagePlaceholder` 关系**  
   - **替换**：主路径由「壳层 + 子页」承担；原 `HomePagePlaceholder` 可删除或改为「首页」Tab 的第一版内容容器（本变更以文字占位为主，推荐壳层内首页 Tab 直接放占位 `Text`，删除对 `HomePagePlaceholder` 的顶层依赖以减少一层）。

4. **图标**  
   - 使用 `Icons.*` 线框风格与设计稿语义对齐（home、calendar、history、person）；不引入新图标包。

## Risks / Trade-offs

- **[Risk] StatefulShellRoute 配置错误导致重定向循环** → 在 `redirect` 中仅匹配壳层前缀，子路径不参与登录外跳；单测 + 手动走登录流验证。  
- **[Trade-off] 首屏默认 Tab** → 固定为「首页」branch index 0，与设计一致。  
- **[Risk] 与现有测试引用 `HomePagePlaceholder`** → 更新或替换 `home_page_placeholder_test` 为壳层/Tab 测试。

## Migration Plan

- 开发：本地替换路由与页面；无服务端迁移。  
- 回滚：恢复 `app_router` 中单 `GoRoute` + `HomePagePlaceholder` 提交即可。

## Open Questions

- Tab 子路径最终命名（`/home` vs `/app/home`）可与产品统一；本设计建议保持 `/home` 为壳根，子路径 `/home/record` 等。
