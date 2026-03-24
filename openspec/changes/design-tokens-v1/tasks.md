## 1. Token 基础设施（Flutter）

- [x] 1.1 在 `apps/mobile/lib/core/`（或 `app/theme/`，与团队约定一致）新增 `app_colors.dart`，`AppColors` 与 spec 中色值完全一致
- [x] 1.2 新增 `app_spacing.dart`，`AppSpacing` 与 spec 中间距 Token 一致
- [x] 1.3 新增 `app_radius.dart`，`AppRadius` 与 spec 中圆角 Token 一致
- [x] 1.4 新增 `app_typography.dart`（或等价），将 `displayLarge`、`numberLarge`、`titleLarge` 等 TextStyle 封装为静态/工厂方法，数值与 spec 一致
- [x] 1.5 新增 `app_shadows.dart`（或主题扩展），实现 `shadowCard`、`shadowFloating`（颜色 #000000、opacity/blur/offset 与 spec 一致）

## 2. 主题与 Material 对齐

- [x] 2.1 在 `app/` 或 `main.dart` 中配置 `ThemeData`，`scaffoldBackgroundColor`、`colorScheme` 等优先映射到 Token（避免与 `bgPrimary`/`primary` 冲突）
- [x] 2.2 文档化：`README` 或 `docs/` 中增加指向 `openspec/specs/design-tokens` 的链接（归档后更新路径）

## 3. 首批组件（TDD：先测后码）

- [x] 3.1 ShijiButton：编写 Widget 测试（启用/禁用、主色与高度 52），再实现组件
- [x] 3.2 ShijiInput：编写 Widget 测试（占位色、高度 48、背景 `bgSecondary`），再实现组件
- [x] 3.3 ShijiCard：编写 Widget 测试（圆角 `radiusLg`、padding 20、背景 `bgCard`），再实现组件
- [x] 3.4 ShijiChip：编写 Widget 测试（默认/选中态颜色与 `radiusPill`），再实现组件
- [x] 3.5 SectionTitle：编写 Widget 测试（层级使用 `titleSmall` 或约定样式），再实现组件
- [x] 3.6 CaptureCard：编写 Widget 测试（高度 172、`radiusXl`、主色背景），再实现组件
- [x] 3.7 CalorieProgressCard：编写 Widget 测试（进度条底/前景色与数字样式），再实现组件
- [x] 3.8 MealRecordCard：编写 Widget 测试（compact/full 图片尺寸与圆角），再实现组件
- [x] 3.9 AIInsightCard：编写 Widget 测试（外层 `accentWarm`、内层 `accentWarmInner`、icon 区色），再实现组件

## 4. 规范与文档同步

- [x] 4.1 将 `apps/mobile/UI设计规范.md` 顶部增加说明：以 OpenSpec `design-tokens` 为准，冲突时以 spec 为准
- [x] 4.2 执行 `/opsx:archive` 或等价流程归档本 change，使 `openspec/specs/design-tokens/spec.md` 与 `client-architecture` 增量合并入主 specs

## 5. 可选质量门禁

- [x] 5.1（可选）增加脚本或分析选项，扫描 `features/` 下非法 `Color(0x`（排除 `app_colors.dart`），在 CI 或本地说明用法
