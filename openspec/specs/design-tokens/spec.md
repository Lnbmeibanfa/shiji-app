# Design Token 规范（design-tokens）

本规范定义食迹移动端 Design Token v1，为 Flutter、AI 生成与 Figma 的唯一色与尺寸事实来源（与 `apps/mobile/lib/core/theme/` 实现应对齐）。

---

## ADDED Requirements

### Requirement: 品牌基调与禁止项

系统 MUST 将 UI 气质约束为：温和、克制、稳定、有呼吸感、轻疗愈、不评判、不高压。界面 MUST 使用浅色背景、低饱和绿色、柔和橙色点缀、大圆角、轻阴影与清晰层级。

系统 MUST NOT 使用：刺眼纯白大面积背景作为主背景、高饱和主绿、鲜红（如 `#FF0000`）作为预警、复杂渐变、重阴影、密集排版、过度花哨插画。

#### Scenario: 主背景与卡片

- **WHEN** 开发者实现页面背景与主卡片背景
- **THEN** MUST 使用 Token `bgPrimary` 与 `bgCard`（或规范中定义的等价 Token），MUST NOT 使用 `#FFFFFF` 作为整页唯一底色造成刺眼感

#### Scenario: 风险与超出提示

- **WHEN** 系统需提示风险、超标或负面健康信号
- **THEN** MUST 使用 `dangerSoft` 或 `warning` 等规范已定功能色，MUST NOT 使用亮红 `#FF0000` 或同等高刺激红色作为主强调

---

### Requirement: 颜色 Token 为唯一色源

所有界面颜色 MUST 仅通过命名 Token 引用。Flutter 代码 MUST 通过 `AppColors`（或主题扩展中映射到相同数值的别名）访问颜色，MUST NOT 在组件 `build` 方法或样式中直接书写十六进制或 `Color(0xFF......)`（`AppColors` 定义文件内部除外）。

#### Scenario: 主色与按压态

- **WHEN** 实现主按钮、选中态、主拍照卡背景
- **THEN** MUST 使用 `primary`；按压态 MUST 使用 `primaryPressed`

#### Scenario: 背景与文本

- **WHEN** 实现页面、卡片、输入区、弱化区背景
- **THEN** MUST 从 `bgPrimary`、`bgCard`、`bgSecondary`、`bgMuted` 中选择，且文本 MUST 使用 `textPrimary` / `textSecondary` / `textTertiary` / `textInverse` 之一

#### Scenario: 边框与分割

- **WHEN** 使用边框或分割线
- **THEN** MUST 使用 `borderLight` 或 `divider`

#### Scenario: 功能色与标签色

- **WHEN** 表示成功、警告、AI 暖色块、轻风险或标签
- **THEN** MUST 仅使用规范已定：`success`、`warning`、`accentWarm`、`accentWarmInner`、`dangerSoft` 及 `tagGreen*`、`tagOrange*`、`tagYellow*`、`tagNeutral*`

#### Scenario: 反馈表面

- **WHEN** 实现 Toast、Banner 或 `client-feedback` 规范中的确认 Dialog
- **THEN** 背景与主操作色 MUST 使用 `feedbackToastSuccess`、`feedbackToastFailure`、`feedbackToastHintFrosted`、`feedbackToastHintWarm`、`feedbackBannerBackground` 等反馈 Token，MUST NOT 在组件内另写与定稿不一致的 hex

---

### Requirement: 反馈表面专用颜色 Token

系统 MUST 在颜色 Token 表中增加 **反馈表面** 语义色，供 Toast、Banner、确认 Dialog 主按钮等使用，与现有 `success` / `warning` 等并存；数值 MUST 与下列十六进制一致（实现写在 `AppColors` 内，业务仅引用 Token 名）。

- `feedbackToastSuccess`：`#95AB99`（成功 Toast 与 Dialog 主确认按钮背景）
- `feedbackToastFailure`：`#E99B76`（失败 Toast，温和暖调，非刺红）
- `feedbackToastHintFrosted`：浅底 `#FAFAF9`，配合毛玻璃/半透明效果（具体 alpha 由实现与主题决定，但实色部分 MUST 对应该 Token）
- `feedbackToastHintWarm`：`#E9BC9C`（暖色实底提示）
- `feedbackBannerBackground`：`#FFF4E6`（公告 Banner 背景）

#### Scenario: Flutter 引用

- **WHEN** 开发者实现 `client-feedback` 规范中的 Toast、Banner 或 Dialog
- **THEN** 背景与按钮色 MUST 使用上述 Token 名之一，MUST NOT 在组件内写死与上表冲突的 hex

#### Scenario: 速查表一致

- **WHEN** 更新主规范中的颜色速查表
- **THEN** MUST 包含本要求中列出的反馈 Token 与 hex

---

### Requirement: 字体层级 Token

字号、字重与行高 MUST 通过规范定义的语义 Token 使用（如 `displayLarge`、`numberLarge`、`titleLarge`、`titleMedium`、`titleSmall`、`bodyLarge`、`bodyMedium`、`bodySmall`、`labelMedium`、`buttonText`）。未另行指定字体族时，MUST 使用系统默认字体。

#### Scenario: 页面主标题与数据

- **WHEN** 渲染首页问候、复盘页标题
- **THEN** MUST 使用 `displayLarge`（28 / 600 / 行高 1.25）

#### Scenario: kcal 与大数字

- **WHEN** 渲染 kcal 或统计大数字
- **THEN** MUST 使用 `numberLarge`（24 / 600 / 行高 1.2）

#### Scenario: 按钮文案

- **WHEN** 渲染主按钮或次按钮内文字
- **THEN** MUST 使用 `buttonText`（16 / 500 / 行高 1.2）

---

### Requirement: 圆角 Token

圆角 MUST 仅使用：`radiusXs` 8、`radiusSm` 12、`radiusMd` 16、`radiusLg` 20、`radiusXl` 24、`radiusPill` 999。MUST NOT 在组件中随意使用未在规范中定义的圆角数值。

#### Scenario: 输入框与按钮

- **WHEN** 实现输入框或按钮圆角
- **THEN** MUST 使用 `radiusMd`（16）

#### Scenario: 通用卡片与拍照主卡

- **WHEN** 实现通用卡片或首页大拍照卡
- **THEN** 通用卡片 MUST 使用 `radiusLg`（20）；首页大拍照卡 MUST 使用 `radiusXl`（24）

#### Scenario: Chip

- **WHEN** 实现 Chip 类组件
- **THEN** MUST 使用 `radiusPill`（999）

---

### Requirement: 间距 Token

间距 MUST 仅使用：`space4` 4、`space8` 8、`space12` 12、`space16` 16、`space20` 20、`space24` 24、`space28` 28、`space32` 32、`space40` 40。

#### Scenario: 页面水平边距

- **WHEN** 设置页面左右外边距
- **THEN** MUST 使用 `space24`（24）

#### Scenario: 卡片内边距

- **WHEN** 设置普通卡片或紧凑卡片内边距
- **THEN** 普通卡片 MUST 使用 `space20`（20）；紧凑卡片 MUST 使用 `space16`（16）

#### Scenario: 模块与列表

- **WHEN** 设置大模块之间或小模块之间垂直间距
- **THEN** 大模块 MUST 使用 `space24`；小模块 MUST 使用 `space16`

#### Scenario: 列表项与图文间距

- **WHEN** 设置列表项间距或图片与文字间距
- **THEN** MUST 默认使用 `space12`（12）

---

### Requirement: 阴影 Token

阴影 MUST 克制使用。`shadowCard` 与 `shadowFloating` 的参数 MUST 与规范一致（卡片：`opacity` 0.04、`blur` 16、`offsetY` 4；浮层：`opacity` 0.06、`blur` 24、`offsetY` 8）。普通卡片 SHOULD 优先浅背景加轻边框，MUST NOT 为每个卡片强制叠加重阴影。

#### Scenario: 重要卡片

- **WHEN** 为首页主拍照卡或重要卡片添加阴影
- **THEN** MUST 使用 `shadowCard` 或 `shadowFloating` 之一，且 MUST NOT 提高 opacity 至规范以上造成「后台感」重阴影

---

### Requirement: 组件尺寸与图标尺寸

按钮高度：主按钮 MUST 为 52；次按钮 MUST 为 44。单行输入框与验证码输入框高度 MUST 为 48；多行备注最小高度 MUST 为 104。底部导航栏高度 MUST 为 64。首页拍照卡高度建议 MUST 为 172；最近记录紧凑卡高度 MUST 为 112；食物行最小高度 MUST 为 64。

图标尺寸 MUST 使用：小 16、常规 20、中 24、大 32、首页拍照 36。图标风格 MUST 为线性、简洁，MUST NOT 厚重描边或拟物。

#### Scenario: 主按钮高度

- **WHEN** 实现 Primary 主按钮
- **THEN** 高度 MUST 为 52

---

### Requirement: 命名组件视觉规则

以下组件视觉 MUST 与规范一致（颜色均指 Token）：

- **ShijiButton Primary**：背景 `primary`，文字 `textInverse`，圆角 `radiusMd`，高度 52；Disabled：背景 `#C9D3C8`，文字 `#F7F8F6`（若后续纳入 Token 表则改为 Token 名）。
- **ShijiInput**：背景 `bgSecondary`，无边框，圆角 `radiusMd`，高度 48，占位 `textTertiary`，输入文字 `textPrimary`。
- **ShijiCard**：背景 `bgCard`，圆角 `radiusLg`，padding 20，可选 `shadowCard` 或无边框轻 `borderLight`。
- **ShijiChip**：默认 `tagNeutralBg`/`tagNeutralText`，圆角 `radiusPill`，高度 36；选中背景 `primary` 文字 `textInverse`。
- **CaptureCard**：背景 `primary`，圆角 `radiusXl`，高度 172，icon 圆底 rgba(255,255,255,0.2)，主文案白色。
- **CalorieProgressCard**：外层 `bgCard`，圆角 `radiusLg`，padding 20，kcal `numberLarge`，进度条底 `#D9DED6`、前景 `primary`。
- **MealRecordCard**：背景 `bgCard`，圆角 `radiusLg`，图片圆角 `radiusMd`，内边距 16，图片 compact 76×76、full 96×96。
- **AIInsightCard**：外层 `accentWarm`，圆角 `radiusLg`，padding 20；icon 区背景 `#F6E8D7`，icon 色 `warning`；标题 `titleMedium` `textPrimary`；正文 `bodyLarge` `textPrimary`；内层建议块 `accentWarmInner`，圆角 `radiusMd`，padding 16。
- **WeeklyChartCard**：背景 `bgCard`，圆角 `radiusLg`，柱子达标 `primary`、超出 `warning`。
- **SettingMenuCell**：高度 56，左右 padding 20，icon 20，文本 `bodyLarge`，箭头 `textTertiary`。

#### Scenario: AI 建议卡气质

- **WHEN** 实现 AIInsightCard
- **THEN** MUST 使用上述外层与内层暖色结构，MUST 呈现「温柔但有方向感」的提醒，MUST NOT 使用冷冰冰系统通知式高对比红蓝块

---

### Requirement: Flutter 文件命名与 AI 生成约束

项目 SHOULD 提供 `app_colors.dart`、`app_spacing.dart`、`app_radius.dart`，其常量命名 MUST 与规范 Token 名一致或可查表映射。使用 AI 生成 Flutter 组件时，提示词 MUST 要求：仅使用 `AppColors` / `AppSpacing` / `AppRadius`（及排版 Token 实现类），禁止在组件内写死颜色、字号、间距、圆角；风格温和轻盈；通用组件参数可复用、业务文案不写死。

#### Scenario: 首批组件优先级

- **WHEN** 启动第一批通用 UI 实现
- **THEN** SHOULD 按顺序落地：ShijiButton、ShijiInput、ShijiCard、ShijiChip、SectionTitle、CaptureCard、CalorieProgressCard、MealRecordCard、AIInsightCard

---

## 颜色与 Token 速查表（规范性引用）

以下数值与《食迹 Design Token 规范 v1》一致，实现 MUST 与此表一致。

**主色板：** primary `#9AAF99`， primaryPressed `#8EA38D`， primarySoft `#DCE7DC`， primarySoftest `#EEF4EE`  
**背景：** bgPrimary `#F6F7F4`， bgCard `#FCFCFA`， bgSecondary `#F0F2EE`， bgMuted `#E9ECE7`  
**文本：** textPrimary `#2F2A24`， textSecondary `#6F685F`， textTertiary `#A19A90`， textInverse `#FFFFFF`  
**线：** borderLight `#E6E9E3`， divider `#ECEFE9`  
**功能：** success `#8DAA8C`， warning `#E2B48F`， accentWarm `#F3E3CC`， accentWarmInner `#FBF4EA`， dangerSoft `#D9A8A0`  
**标签：** tagGreenBg `#EEF5EE` / tagGreenText `#7B9A7B`； tagOrangeBg `#FBF0E5` / tagOrangeText `#D49A6A`； tagYellowBg `#F8F3E3` / tagYellowText `#B89B4D`； tagNeutralBg `#F1F2EF` / tagNeutralText `#7A746C`  
**反馈表面：** feedbackToastSuccess `#95AB99`； feedbackToastFailure `#E99B76`； feedbackToastHintFrosted 浅底 `#FAFAF9`（毛玻璃/半透明由实现叠加）； feedbackToastHintWarm `#E9BC9C`； feedbackBannerBackground `#FFF4E6`

**Flutter 参考实现：** `apps/mobile/lib/core/theme/`（`AppColors`、`AppSpacing`、`AppRadius`、`AppTypography`、`AppShadows`、`AppSizes`）。
