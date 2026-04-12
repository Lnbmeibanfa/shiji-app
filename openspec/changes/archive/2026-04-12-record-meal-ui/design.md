## Context

- 设计稿已定义「记录饮食」页：照片区、餐别、最近餐、手动添加、情绪、备注、保存。
- 现有实现：`apps/mobile` 中 `CameraPage` + `/camera` + 上传 Repository；后端已有 `POST /api/meal-records`。
- 用户明确：**不引入 AI**；**「最近一餐」数据与逻辑延后**，页面先展示占位；**保存**需至少一条食物项。

## Goals / Non-Goals

**Goals:**

- 全屏路由与产品语义统一为「记录饮食」，路径与常量命名与「camera」解耦。
- 按模块拆分组件，样式对齐现有 `AppColors` / `AppSpacing` / `AppRadius` / `AppTypography`。
- 页面级状态：`mealType`、`recordedAt`（默认 `DateTime.now()` 即可）、`fileId?`（来自上传）、`foodItems`（列表）、`emotionSelections`、`note`；**保存按钮**在 `foodItems.isEmpty` 时禁用（视觉置灰）。
- 提交模型约定（与后端对齐）：**无照片**时 `record_method = 'manual'`；**有照片**时 `record_method = 'photo'`（与 `meal_record.record_method` 枚举一致）。

**Non-Goals:**

- 最近一餐列表的真实数据、分页、点击回填（仅占位 UI）。
- AI 识别、食物库检索、与后端保存接口的完整联调（可后续单独变更；本变更 tasks 中「接线」可为占位或 TODO）。
- 手动添加食物的完整子页（首版可用 `showModalBottomSheet` / 占位表单 + 本地 `foodItems` 追加）。

## Decisions

### 1. 路由与命名

- **路径**：`RoutePaths.recordMeal = '/record-meal'`（或等价 kebab-case，全仓库唯一）。
- **页面类名**：`RecordMealPage`（或 `RecordDietPage`，实现时二选一并与文件名一致）。
- **目录**：优先 `features/record_meal/`（从 `features/camera` 迁移或重导出），避免长期保留 `camera` 命名。
- **理由**：`camera` 暗示仅相机能力；产品名是「记录饮食」。

### 2. 组件拆分（先拆后写再拼）

| 组件（建议文件名） | 职责 |
|-------------------|------|
| `RecordMealPhotoSection` | 选图/拍照、上传、预览、清除；对外 `onUploaded(FileUploadResponse?)` / `fileId` |
| `MealTypeSelector` | 四餐：早餐/午餐/晚餐/加餐；单选；映射 `meal_type` 字符串（`breakfast` / `lunch` / `dinner` / `snack`） |
| `RecentMealsPlaceholder` | 标题「最近的{餐别}」+ 空态/占位卡片（无 API） |
| `ManualAddFoodButton` | 主按钮样式「+ 手动添加食物」；打开 bottom sheet 或占位页，向父级 `onAddFood` 回调 |
| `EmotionChipsSection` | 可选多选；数据首版本地常量（emoji + 文案），**不强制**对接 `emotion_tag_id`；预留 `List<int>?` 或后续同步 |
| `MealNotesField` | 多行备注 |
| `RecordMealSaveBar` | 底栏「保存记录」；`onPressed` 仅在 `canSave` 时非空；`canSave = foodItems.isNotEmpty` |

**页面**：`RecordMealPage` 仅 `Scaffold` + `SingleChildScrollView` 组合上述组件，持有 `State` / `Notifier`.

### 3. `record_method` 与照片

- **无照片**（未上传成功或已清除）：`record_method = 'manual'`。
- **有有效 `fileId`**：`record_method = 'photo'`。

### 4. 保存按钮

- **MUST**：`foodItems.length` 为 0 时，主按钮 `onPressed == null` 且使用禁用/置灰样式（`ButtonStyle` 或封装 `opacity` + 语义色）。

### 5. 与 `go_router` 集成

- 顶层全屏 `GoRoute` 与 Tab 壳同级：`path: RoutePaths.recordMeal`，`builder` 指向 `RecordMealPage`。
- 首页 `CaptureCard`、Shell 占位 `onPressed` 等：`context.push(RoutePaths.recordMeal)`。

## Risks / Trade-offs

- **[Risk] 情绪标签 ID 与后端不一致** → 首版可只存 UI 选中态，保存前再映射；或文档中写「保存接口接线前不提交情绪」。
- **[Risk] 重命名路由破坏书签/测试** → 全局搜索替换 `RoutePaths.camera` / `'/camera'`。

## Migration Plan

- 代码中所有 `RoutePaths.camera` 与 `/camera` 改为 `RoutePaths.recordMeal`。
- 若存在文档或 OpenSpec 引用旧路径，一并更新。

## Open Questions

- （暂无）手动添加食物的具体字段与校验可在后续「食物编辑」变更中细化。
