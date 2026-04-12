## 1. 路由与目录命名

- [x] 1.1 将 `RoutePaths.camera` / `'/camera'` 改为 `RoutePaths.recordMeal` / `'/record-meal'`，并更新 `app_router.dart`、首页、`shell_tab_placeholders` 等所有引用
- [x] 1.2 将 `CameraPage` 重命名为 `RecordMealPage`（或等价），并调整 `features` 目录为 `record_meal/`（或保留 `camera` 目录但导出同名页面，二选一与 design 一致）

## 2. 拆分组件（先文件与 API，再实现 UI）

- [x] 2.1 新增 `RecordMealPhotoSection`：承接原上传/预览/清除逻辑，回调 `fileId` / `FileUploadResponse?`
- [x] 2.2 新增 `MealTypeSelector`：四餐单选，输出 `meal_type` 字符串
- [x] 2.3 新增 `RecentMealsPlaceholder`：标题随餐别变化 + 占位内容（无 API）
- [x] 2.4 新增 `ManualAddFoodButton`：打开 bottom sheet 或占位页，向父级追加 `foodItems`（首版可只加一条 mock 食物）
- [x] 2.5 新增 `EmotionChipsSection`：本地常量 chips，多选状态由父级持有
- [x] 2.6 新增 `MealNotesField`：备注 `TextField`
- [x] 2.7 新增 `RecordMealSaveBar`：`canSave = foodItems.isNotEmpty`，禁用时置灰

## 3. 组装页面与状态

- [x] 3.1 实现 `RecordMealPage`：`SingleChildScrollView` + 上述组件；`State` 含 `foodItems`、`mealType`、`note`、情绪选中、`fileId?`
- [x] 3.2 实现 `record_method` 规则：无 `fileId` → `manual`；有 `fileId` → `photo`（为后续 `POST /api/meal-records` 准备）
- [x] 3.3 确认 AppBar 标题为「记录饮食」，返回行为为 `pop`

## 4. 联调与收尾

- [x] 4.1 （可选）接入 `POST /api/meal-records`：用 `ApiClient` + Repository，成功/失败走 `AppFeedback`
- [x] 4.2 对照 `specs/record-meal-ui/spec.md` 与 `specs/mobile-home-ui/spec.md` 自测导航与按钮禁用逻辑
