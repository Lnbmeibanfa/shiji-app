## Why

视觉识别在词表匹配阶段已解析出标准 `food_item`（如 id=7 对应「鸡肉」），但 HTTP 响应里的 `ingredients[]` 仅包含 `ingredientId` 与 `confidence`，前端无法直接展示名称与热量等常用信息，只能占位或二次请求。应在**同一响应**中附带只读展示字段，减少往返并与保存时的 snapshot 思路一致。

## What Changes

- 扩展 **`DishIngredientVisionResponse.recognition.ingredients[]`** 中每条食材：在保留 `ingredientId`、`confidence` 的前提下，增加 **`foodName`** 及用于回显/估算的 **`defaultUnit`**、**`caloriesPer100g`**（可选，无营养数据时为 null）。
- 同步接口 `POST /api/ai/dashscope/vision/dish-ingredients` 与异步 poll 返回的 **`result`（同结构）** 一并遵循该形状。
- 字段语义为**识别时刻**自标准库解析的只读快照；**业务主键仍以 `ingredientId` 为准**。
- 非 **BREAKING**：纯新增字段，旧客户端可忽略。

## Capabilities

### New Capabilities

- `dish-ingredient-vision-response`：定义菜品/食材视觉识别 JSON 中 `ingredients[]` 的展示字段契约及与 `food_item` / 营养数据的对应关系。

### Modified Capabilities

- （无）全局基线 spec 中未单独锁定该 JSON 细项；本变更以新 capability 为权威，后续可与 `meal-photo-recognition-async` 等变更交叉引用。

## Impact

- **后端**：`DishIngredientVisionResponse.Ingredient`、`DishIngredientVisionService` 映射逻辑；可选在词表或映射阶段批量解析 `per_100g` 热量。
- **客户端**：Flutter（及任意消费者）解析模型与记录页草稿展示逻辑，可直接使用 `foodName` / `caloriesPer100g`。
- **文档**：`services/api/README.md` 视觉识别与异步识别章节中补充字段说明。
- **测试**：视觉与异步识别相关集成测试中断言新字段（至少 `foodName`）。
