> **规范优先级**：界面与 Token 的**唯一事实来源**以 OpenSpec 为准：  
> - 归档路径：`openspec/specs/design-tokens/spec.md`  
> - 变更未归档前：`openspec/changes/design-tokens-v1/specs/design-tokens/spec.md`  
> 本文档与 OpenSpec **冲突时，以 OpenSpec `design-tokens` 规范为准**。

《食迹组件库规范 v1》
目标：
把当前 UI 稿沉淀成一套可复用、可扩展、可让 AI 稳定生成代码的组件系统。
这份规范主要解决 4 件事：

1.  保证视觉统一
2.  保证页面开发速度
3.  保证 AI 生成代码不跑偏
4.  保证后续改版成本低

一、设计系统总原则
食迹的组件系统必须始终服务于产品人格：
温和、稳定、克制、治愈、轻陪伴
因此组件设计遵循以下原则：

1. 少而稳
   组件数量不要过多，优先抽高频基础组件。
2. 统一优先
   宁可组件样式有一点重复，也不要在页面里随意“临时发挥”。
3. 页面由组件拼成
   页面不直接写大段样式，优先由组件组合。
4. 先通用，后业务
   先做基础组件，再做食迹特色业务组件。
5. token 驱动
   颜色、字号、间距、圆角、阴影不允许散落在页面中写死。

二、组件系统分层
食迹组件库分 4 层：
L1 设计 Token 层
L2 基础组件层
L3 业务组件层
L4 页面模板层

L1：设计 Token 层
包括：
● 颜色
● 字体
● 间距
● 圆角
● 阴影
● 图标尺寸

L2：基础组件层
包括：
● Button
● Card
● Chip
● Input
● SectionTitle
● TopBar
● EmptyState

L3：业务组件层
包括：
● CaptureCard
● CalorieProgressCard
● MealRecordCard
● AIInsightCard
● FoodItemRow
● WeeklyChartCard
● StatisticCard
● ProfileSummaryCard

L4：页面模板层
包括：
● AppScaffold
● ListPageScaffold
● DetailPageScaffold

三、Design Token 规范
这是最核心的技术规范。
以后 AI 生成 Flutter 代码时，必须只使用这里定义的 token。

3.1 颜色规范
建议统一放在：
/lib/core/theme/app_colors.dart
主色
● primary：食迹主绿
用途：主按钮、核心 CTA、选中态
● primarySoft：浅主绿
用途：浅背景、图标底、卡片点缀
中性色
● bgPrimary：全局背景
● bgCard：卡片背景
● bgSecondary：浅灰背景
● borderLight：轻边框
● divider：分割线
文本色
● textPrimary
● textSecondary
● textTertiary
● textInverse
状态色
● success
● warning
● dangerSoft
● infoSoft
标签色
● tagGreenBg
● tagGreenText
● tagOrangeBg
● tagOrangeText
● tagYellowBg
● tagYellowText
AI 反馈色
● insightBg
● insightInnerBg
● insightIconBg

3.2 字体规范
建议统一放在：
/lib/core/theme/app_text_styles.dart
字号层级
● displayLarge：首页主数字，如 kcal
● titleLarge：页面主标题
● titleMedium：区块标题
● bodyLarge：常规正文
● bodyMedium：列表正文
● bodySmall：辅助文字
● labelLarge：按钮文字
● labelSmall：标签文字
字重原则
● 页面大标题：SemiBold
● 普通标题：Medium / SemiBold
● 正文：Regular
● 数据数字：Medium / SemiBold
● 标签文字：Medium
⚠️ 不允许页面中随意写：
● fontSize: 17
● fontWeight: w537
之类的零散样式。

3.3 间距规范
建议统一放在：
/lib/core/theme/app_spacing.dart
定义：
● xs
● sm
● md
● lg
● xl
● xxl
使用规则：
● 组件内小间距：xs / sm
● 卡片内边距：md / lg
● 页面左右 padding：lg
● 模块上下间距：lg / xl

3.4 圆角规范
建议统一放在：
/lib/core/theme/app_radius.dart
定义：
● sm
● md
● lg
● xl
● pill
建议使用：
● 输入框：md / lg
● 通用卡片：xl
● 按钮：lg / xl
● 标签：pill

3.5 阴影规范
建议统一放在：
/lib/core/theme/app_shadows.dart
原则：
● 只允许使用 1~2 级轻阴影
● 不要使用重阴影
● 大部分卡片可以仅靠背景色 + 极浅边框来区分层次
定义：
● card
● floating

四、页面骨架组件规范

4.1 AppScaffold
用途
全局页面容器，统一：
● 安全区
● 背景色
● 默认滚动方式
● 底部导航兼容
文件建议
/lib/shared/scaffold/app_scaffold.dart
参数
● title
● body
● backgroundColor
● padding
● bottomNavigationBar
● showAppBar
● appBarLeading
● appBarActions
使用规则
所有页面优先使用 AppScaffold 包裹，不直接裸写 Scaffold。

4.2 ListPageScaffold
用于：
● 首页
● 记录页
● 我的页
● 复盘页
特征：
● 支持滚动
● 支持顶部标题区域
● 支持多个 section

4.3 DetailPageScaffold
用于：
● 餐次详情页
● 确认食物页
特征：
● 标题栏固定
● 主内容滚动
● 底部固定按钮可选

五、基础组件规范（L2）

5.1 ShijiButton
组件名
ShijiButton
用途
统一食迹所有按钮样式。
变体
● primary
● secondary
● ghost
● dangerSoft
状态
● 默认
● 禁用
● loading
参数
● text
● onPressed
● variant
● isLoading
● isDisabled
● icon
● fullWidth
视觉规则
● 主按钮使用主绿底
● 禁用态降低饱和度
● 圆角统一偏大
● 高度统一
不允许
● 页面中自己手写一套按钮样式

5.2 ShijiCard
组件名
ShijiCard
用途
统一所有卡片外壳样式。
参数
● child
● padding
● backgroundColor
● radius
● border
● shadow
● onTap
使用规则
所有业务卡片都尽量基于 ShijiCard 扩展，而不是重新写 Container + BoxDecoration。

5.3 ShijiChip
组件名
ShijiChip
用途
统一标签展示。
变体
● default
● selected
● success
● warning
● emotion
参数
● text
● icon
● selected
● variant
● onTap
使用场景
● 餐别选择
● 情绪标签
● 健康标签

5.4 ShijiInput
组件名
ShijiInput
用途
统一输入框样式。
参数
● hintText
● controller
● keyboardType
● suffix
● prefix
● maxLines
● readOnly
使用规则
● 登录页手机号输入
● 验证码输入
● 备注输入

5.5 SectionTitle
组件名
SectionTitle
用途
统一区块标题区域。
结构
左侧：
● 标题
● 可选副标题
右侧：
● 操作按钮 / “查看全部”
参数
● title
● subtitle
● actionText
● onActionTap
使用场景
● 最近记录
● 本周高频食物
● 健康标签

5.6 ShijiTopBar
用途
统一顶部导航栏风格。
参数
● title
● leading
● actions
● centerTitle
使用场景
● 确认食物页
● 午餐详情页
● 记录饮食页

5.7 EmptyState
用途
统一空状态页面。
参数
● icon
● title
● desc
● buttonText
● onButtonTap
使用场景
● 无记录
● 无复盘
● 加载失败

六、业务组件规范（L3）
这是食迹最关键的层。

6.1 CaptureCard
组件名
CaptureCard
用途
首页最核心入口卡。
组成
● 图标/相机 icon
● 主文案：“拍一顿”
● 可选副文案
参数
● title
● subtitle
● onTap
● icon
设计要求
● 是首页最显眼的区域
● 一眼知道可以拍照记录
● 不要复杂装饰
页面使用
● 首页

6.2 CalorieProgressCard
组件名
CalorieProgressCard
用途
展示今日热量摄入情况。
组成
● 今日已摄入
● 目标值
● 剩余值
● 进度条
● 可选三大营养素概览
参数
● consumedKcal
● targetKcal
● remainingKcal
● carb
● protein
● fat
● showMacro
使用场景
● 首页
● 记录页顶部
● 周复盘摘要区

6.3 MealRecordCard
组件名
MealRecordCard
用途
展示一条餐次记录。
组成
● 图片
● 餐名/食物名
● 餐别/时间
● kcal
● 一句简短分析
参数
● imageUrl
● title
● subtitle
● mealType
● time
● kcal
● briefComment
● onTap
变体
● compact：首页最近记录
● full：记录页大卡片
设计要求
首页和记录页应尽量使用同一个组件的不同尺寸变体，而不是两个不同组件。

6.4 AIInsightCard
组件名
AIInsightCard
用途
展示食迹最重要的 AI 温柔反馈。
组成
● 左上图标 / 小标识
● 主标题，如“这一餐吃得很棒”
● 正文反馈
● 内嵌建议模块（可选）
● 建议标题，如“小建议”
参数
● title
● content
● suggestionTitle
● suggestionContent
● variant
● icon
变体
● mealFeedback
● dailyTip
● weeklyInsight
设计要求
这个组件必须是品牌型组件。
以后用户一看到这个卡片，就知道这是“食迹在跟我说话”。

6.5 FoodItemRow
组件名
FoodItemRow
用途
展示单个食物条目。
组成
● 食物名
● 重量
● kcal
● 编辑按钮（可选）
● 前置小圆点（可选）
参数
● foodName
● weightText
● kcal
● editable
● onEditTap
使用场景
● 确认食物页
● 餐次详情页

6.6 NutritionSummaryCard
组件名
NutritionSummaryCard
用途
展示一餐的营养摘要。
组成
● 总热量
● 碳水
● 蛋白质
● 脂肪
参数
● totalKcal
● carb
● protein
● fat
使用场景
● 餐次详情页

6.7 WeeklyChartCard
组件名
WeeklyChartCard
用途
周复盘柱状图卡片。
组成
● 平均热量
● 达成率
● 简单图表
● 图例
参数
● avgKcal
● achievementRate
● chartData
设计要求
图表简洁，不要复杂动效和多色彩。

6.8 FoodFrequencyCard
组件名
FoodFrequencyCard
用途
周复盘中的高频食物展示。
组成
● 图标/插画
● 食物名称
● 出现次数
参数
● icon
● title
● countText
页面使用
● 周复盘页

6.9 ProfileSummaryCard
组件名
ProfileSummaryCard
用途
我的页面顶部信息卡。
组成
● 头像
● 用户名
● 打卡天数
● 身高
● 体重
● 目标体重
参数
● avatar
● nickname
● recordDays
● height
● weight
● targetWeight

6.10 SettingMenuCell
组件名
SettingMenuCell
用途
我的页面列表项。
组成
● 左侧图标
● 标题
● 右箭头
参数
● icon
● title
● onTap

七、页面与组件映射关系
这个最重要，直接决定你怎么拼页面。

7.1 登录页
使用：
● AppScaffold
● ShijiInput
● ShijiButton
● ShijiCard（可选）
● ShijiTopBar（通常不需要）

7.2 首页
使用：
● AppScaffold
● CaptureCard
● CalorieProgressCard
● SectionTitle
● MealRecordCard
● AIInsightCard

7.3 记录饮食页
使用：
● DetailPageScaffold
● ShijiChip
● ShijiInput
● ShijiButton

7.4 确认食物页
使用：
● DetailPageScaffold
● ShijiCard
● FoodItemRow
● ShijiButton

7.5 餐次详情页
使用：
● DetailPageScaffold
● NutritionSummaryCard
● ShijiChip
● FoodItemRow
● AIInsightCard

7.6 记录页
使用：
● ListPageScaffold
● CalorieProgressCard
● MealRecordCard

7.7 周复盘页
使用：
● ListPageScaffold
● WeeklyChartCard
● FoodFrequencyCard
● AIInsightCard
● SectionTitle

7.8 我的页
使用：
● ListPageScaffold
● ProfileSummaryCard
● StatisticCard
● SettingMenuCell

八、组件开发优先级
不要全做，按顺序来。

第一批：必须先做

1. AppScaffold
2. ShijiButton
3. ShijiCard
4. ShijiInput
5. SectionTitle
6. ShijiChip
   这批做完，你的基础体系就稳了。

第二批：首页闭环组件

1. CaptureCard
2. CalorieProgressCard
3. MealRecordCard
4. AIInsightCard
   这批做完，你就能高质量拼出首页。

第三批：记录与详情组件

1. FoodItemRow
2. NutritionSummaryCard

第四批：复盘与个人中心组件

1. WeeklyChartCard
2. FoodFrequencyCard
3. ProfileSummaryCard
4. SettingMenuCell

九、Flutter 文件结构建议
建议采用下面的结构：
/lib
├── core
│ ├── theme
│ │ ├── app_colors.dart
│ │ ├── app_text_styles.dart
│ │ ├── app_spacing.dart
│ │ ├── app_radius.dart
│ │ ├── app_shadows.dart
│
├── shared
│ ├── scaffold
│ │ ├── app_scaffold.dart
│ │ ├── list_page_scaffold.dart
│ │ ├── detail_page_scaffold.dart
│ ├── widgets
│ │ ├── shiji_button.dart
│ │ ├── shiji_card.dart
│ │ ├── shiji_chip.dart
│ │ ├── shiji_input.dart
│ │ ├── section_title.dart
│ │ ├── shiji_top_bar.dart
│ │ ├── empty_state.dart
│
├── features
│ ├── home
│ │ ├── widgets
│ │ │ ├── capture_card.dart
│ │ │ ├── calorie_progress_card.dart
│ │ │ ├── meal_record_card.dart
│ │ │ ├── ai_insight_card.dart
│ ├── meal
│ │ ├── widgets
│ │ │ ├── food_item_row.dart
│ │ │ ├── nutrition_summary_card.dart
│ ├── review
│ │ ├── widgets
│ │ │ ├── weekly_chart_card.dart
│ │ │ ├── food_frequency_card.dart
│ ├── profile
│ │ ├── widgets
│ │ │ ├── profile_summary_card.dart
│ │ │ ├── setting_menu_cell.dart

十、AI 生成组件代码的统一提示词模板
以后你让 AI 帮你写 Flutter 组件，不要直接说“写一个卡片”。
统一用下面的模板：

通用 Prompt 模板
请为 Flutter 项目“食迹”生成一个可复用组件代码。
请严格遵守以下要求：

1.  项目风格为：温和、轻盈、治愈、克制的健康陪伴风
2.  只使用统一 design token，不要在组件内部写死颜色、字号、间距、圆角
3.  组件必须适合复用，参数设计清晰
4.  组件代码要易读、易维护
5.  不要写业务请求逻辑，只写纯 UI 组件
6.  优先使用 StatelessWidget，只有必要时才用 StatefulWidget
7.  命名符合 Flutter 最佳实践
8.  样式尽量复用 AppColors、AppTextStyles、AppSpacing、AppRadius
9.  代码中加必要注释，说明组件用途和参数
10. 输出完整可运行组件代码
    组件名称：
    [这里填组件名]
    组件用途：
    [这里填用途]
    必须支持参数：
    [这里填参数]
    参考风格：
    ● 大圆角
    ● 浅背景
    ● 轻阴影
    ● 信息层级清晰
    ● 不制造焦虑

十一、组件验收标准
你每做完一个组件，都要过这 5 条：

1. 是否复用 token
   不能写死样式值。
2. 是否参数清晰
   组件不能只能用于一个死场景。
3. 是否风格统一
   是否符合食迹整体调性。
4. 是否可扩展
   后续需求变动时，不需要重写。
5. 是否页面已能复用
   至少能在两个地方被复用，才说明抽象合理。

十二、你现在该怎么执行
最优顺序是：
第一步
先建 token 文件：
● colors
● text styles
● spacing
● radius
● shadows
第二步
做基础组件：
● ShijiButton
● ShijiCard
● ShijiInput
● ShijiChip
● SectionTitle
第三步
做首页核心组件：
● CaptureCard
● CalorieProgressCard
● MealRecordCard
● AIInsightCard
第四步
拼首页
