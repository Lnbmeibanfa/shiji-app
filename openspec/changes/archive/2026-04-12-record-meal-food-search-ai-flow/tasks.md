## 1. 后端：标准食物分页搜索

- [x] 1.1 新增 `GET` 端点（路径以最终实现为准，建议 `/api/food-items`）：查询参数 `q`、`page`（0-based）、`size`，仅 `edible_status=1`。
- [x] 1.2 实现 `food_name` 模糊查询（可选扩展 `food_alias`）；联结或子查询 `food_nutrition` 取 `nutrient_basis=per_100g` 的 `calories` 作为 `caloriesPer100g`。
- [x] 1.3 定义响应 DTO：`items`、`hasNext`（及必要时分页元数据）；统一 `ApiResponse` 成功封装。
- [x] 1.4 参数校验（`page`≥0、`size` 默认 20 且上限 50）；非法参数返回非 0 `code`。
- [x] 1.5 集成测试或 WebMvcTest：未登录 401、有登录 200、分页与 `q` 行为。

## 2. 前端：添加食物 Bottom Sheet 组件

- [x] 2.1 新建组件（如 `AddFoodBottomSheet`）：标题「添加食物」、关闭、圆角搜索框、列表项布局对齐设计稿。
- [x] 2.2 接入 `GET /api/food-items`：防抖（~300ms）、`q` 变化重置 `page=0`、滚动触底加载下一页并追加。
- [x] 2.3 点击行：关闭弹层并 `Navigator.pop`/回调返回选中 `FoodItemSummary`（id、名称、kcal/100g）。
- [x] 2.4 处理加载中、空态、错误提示；列表使用 `ListView` + 滚动监听或 `NotificationListener`。

## 3. 前端：记录饮食状态机与 stub AI

- [x] 3.1 在页面级 State（`Notifier`/等）引入：`idle` | `uploading` | `aiRecognizing` 与布尔闸门；派生 `canSave`。
- [x] 3.2 `uploading`：与现有图片上传流程绑定；上传中保存禁用。
- [x] 3.3 `aiRecognizing`：上传成功后进入；全屏/遮罩 Loading +「打断」按钮；打断 → `idle` 且不追加 stub 行。
- [x] 3.4 Stub：识别「完成」后向 `foodItems` 追加 2～3 条固定 `foodItemId`（与种子数据一致），行 `recognitionSource=ai`，默认重量 100g，热量按 per-100g 计算；行 UI 显示「AI 识别」标签。
- [x] 3.5 手动搜索添加：`recognitionSource=user_manual`；名称只读；可删行、改克重。

## 4. 餐别、保存与后端对齐

- [x] 4.1 进入页按左闭右开区间设置默认 `mealType`（与现有枚举字符串一致）。
- [x] 4.2 组装 `CreateMealRecordRequest`：`recordMethod` 为 `manual` / `photo` / `photo_ai`（stub 路径用 `photo_ai`）；`foodItems` 含正确 `recognitionSource` 与 `sortOrder`。
- [x] 4.3 联调 `POST /api/meal-records`：至少一食物、有图时带 `file_id`；验证成功落库。

## 5. 文档与收尾

- [x] 5.1 `services/api/README.md` 或模块说明中补充新端点路径与参数（若项目惯例要求）。
- [x] 5.2 自测清单：搜索分页、打断识别、仅手动保存、上传+stub+保存。
