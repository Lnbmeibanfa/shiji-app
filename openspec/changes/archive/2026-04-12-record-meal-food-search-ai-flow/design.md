## Context

- 数据库已有：`meal_record.record_method`（`manual` | `photo` | `photo_ai` | `quick_add`），`meal_food_item.recognition_source`（`ai` | `user_manual` | `user_corrected`），`food_item` + `food_nutrition`（热量可按 `nutrient_basis = per_100g` 取值）。
- 后端已实现 `POST /api/meal-records`，`MealFoodItemRequest` 含 `recognitionSource` 等字段。
- 前端已有记录饮食页骨架；需对齐设计稿的「添加食物」底栏与完整状态机。
- 真实 AI 识别未就绪：需 **stub** 跑通「上传 → 识别态 → 列表追加 → 保存」。

## Goals / Non-Goals

**Goals:**

- 提供 **分页 + 名称模糊** 的食物查询 API，列表项展示 **kcal/100g**（与 UI 一致）。
- Flutter：**独立组件**「添加食物」bottom sheet（搜索、列表、触底加载、选中回调）。
- **状态机**（与提案一致）：
  - 进入页：餐别按当前时间 **左闭右开** 区间默认：`[05:00,10:00)` 早餐、`[10:00,15:00)` 午餐、`[15:00,20:00)` 晚餐、`[20:00,次日05:00)` 夜宵。
  - **idle**：可编辑表单。
  - **uploading**：图片上传至可得 `file_id` 过程中，**禁止保存**，按钮置灰。
  - **aiRecognizing**：模拟或未来真实识别进行中，**全屏/遮罩阻塞交互**，提供 **打断** → 回到 idle（不打断则完成后进入 idle）。
  - **canSave** = `foodItems.isNotEmpty` **且** `!uploading` **且** `!aiRecognizing`。
- AI 结果 **直接 append** 到列表；行 UI 标「AI 识别」；用户可删改克重，**不改名称**（与 `food_item` 绑定）。
- 上传成功后的 **stub**：写入若干行，`recognitionSource = ai`，`foodItemId`/快照与种子数据一致；整餐 `recordMethod = photo_ai`（与产品约定：有图且走 AI 路径；纯手动为 `manual`；仅拍照无 AI 为 `photo`——stub 阶段视为 `photo_ai`）。

**Non-Goals:**

- 真实图像识别模型、异步任务、回调 Webhook。
- 用户在前端 **新建** `food_item` 写入库（若后续需要另开变更）。
- 修改 `openspec/specs` 根目录归档（由 archive 流程处理）。

## Decisions

### 1. 餐级 `record_method` vs 行级 `recognition_source`

- **餐级** `record_method`：描述**本餐记录怎么产生**（是否照片、是否 AI、是否快捷添加）。
- **行级** `recognition_source`：描述**该食物行**来自 AI 还是用户手动搜索添加。
- 同一餐可同时存在 `ai` 与 `user_manual` 行；**不合并**同名食物，由用户删除重复项。

### 2. 食物搜索 API 形状（建议）

- `GET /api/food-items`（路径以实现为准，全局唯一）。
- Query：`q`（可选，模糊匹配 `food_name`；可扩展 `food_alias`）、`page`（0-based 或 1-based **在 spec 中写死一种**）、`size`（默认 20，上限如 50）。
- 仅 `edible_status = 1`。
- 响应：`items[]` 含 `id`, `foodName`, `caloriesPer100g`（来自 `food_nutrition` 中 `per_100g` 行；若无则 `null` 或省略，前端展示 `--`）、`hasNext` 或 `total`+分页元数据。

### 3. Stub AI 数据

- 在客户端常量中固定 2～3 个 `food_item_id`（与测试/种子数据一致），识别「结束」后追加到列表，默认重量 100g，热量按 per-100g 计算。
- **打断**：清空本次 stub 任务或保留已上传 `file_id` 由产品定；**建议**：打断仅取消识别态，**不**自动追加 stub 行。

### 4. 搜索防抖与分页重置

- 搜索框输入防抖（如 300ms）后重新请求 **第 1 页**；滚动触底加载 **下一页**，`q` 不变。

## Risks / Trade-offs

- **[Risk]** `food_nutrition` 无 JPA 实体 → 实现用 `@Query` 联结或一次性补实体。**缓解**：任务中明确一种实现方式并加集成测试。
- **[Risk]** stub 使用的 `food_item_id` 在空库不存在 → 开发环境依赖种子 SQL。**缓解**：文档注明或 fallback 仅快照字段（若允许无 `foodItemId` 则与现后端校验冲突，**优先保证种子数据**）。
- **[Trade-off]** `photo` vs `photo_ai` 仅靠前端状态可能不一致 → 以用户最终点击保存时的会话标志为准，并在 design 与 spec 中写清。

## Migration Plan

- 先合并 API，再合并移动端；无 DB 迁移（表已存在）。
- 回滚：移除新端点与前端调用，不影响已有保存接口。

## Open Questions

- `record_method` 中 **`quick_add`** 与本页「搜索添加」是否同一入口；若否，本页默认不发送 `quick_add`（留给其他入口）。
