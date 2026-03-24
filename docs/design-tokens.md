# 食迹 Design Token（索引）

## 规范来源

- **唯一事实来源（需求级）**：[`openspec/specs/design-tokens/spec.md`](../openspec/specs/design-tokens/spec.md)

## Flutter 实现位置

| 内容 | 路径 |
|------|------|
| 颜色 | `apps/mobile/lib/core/theme/app_colors.dart` |
| 间距 | `apps/mobile/lib/core/theme/app_spacing.dart` |
| 圆角 | `apps/mobile/lib/core/theme/app_radius.dart` |
| 排版 | `apps/mobile/lib/core/theme/app_typography.dart` |
| 阴影 | `apps/mobile/lib/core/theme/app_shadows.dart` |
| 组件尺寸 | `apps/mobile/lib/core/theme/app_sizes.dart` |
| Material 主题 | `apps/mobile/lib/core/theme/shiji_theme.dart` |
| 首批 UI 组件 | `apps/mobile/lib/core/widgets/` |

## 相关变更（历史）

- 提案与任务：`openspec/changes/design-tokens-v1/`（归档后仍以主 spec 为准）

## 本地检查（可选）

在仓库根目录执行（需已安装 [ripgrep](https://github.com/BurntSushi/ripgrep)）：

```bash
bash scripts/check_flutter_color_literals.sh
```

用于发现 `features/` 下是否误用 `Color(0x...)`；通过 `AppColors` 引用颜色时应无命中。
