# 菜品/食材视觉响应中的食材展示字段（dish-ingredient-vision-response）

本规范约束 **`DishIngredientVisionResponse.recognition.ingredients[]`** 在词表匹配成功时的 JSON 形状，使客户端无需仅凭 `ingredientId` 即可回显名称与常用营养提示。适用于同步视觉接口及异步任务 poll 返回的 **`result`**（结构对齐同一 DTO）。

---

## ADDED Requirements

### Requirement: 匹配成功的食材项 SHALL 携带展示用快照字段

当某条 `ingredients` 元素由后端自标准 `food_item` 词表匹配生成时，该元素 SHALL 除 `ingredientId`（字符串形式 BIGINT）与 `confidence` 外，还 SHALL 包含：

- `foodName`：与当前匹配所用 `food_item.food_name` 一致的只读展示名；
- `defaultUnit`：与当前 `food_item.default_unit` 一致的只读单位；
- `caloriesPer100g`：当且仅当服务端能解析到该 `food_item` 在 **`per_100g`** 基础上的热量数值时填写；否则 SHALL 为 JSON `null`。

`ingredientId` SHALL 继续作为与标准食物关联的主标识；展示字段 SHALL NOT 替代 `ingredientId` 的语义。

#### Scenario: 词表命中鸡肉

- **WHEN** 模型输出某食材名且后端匹配到 `food_item.id = 7`（展示名「鸡肉」）
- **THEN** 对应 `ingredients[]` 元素 SHALL 包含 `ingredientId` `"7"`、`confidence`，且 SHALL 包含 `foodName` 为「鸡肉」及非空的 `defaultUnit`；若存在 per_100g 热量记录则 `caloriesPer100g` SHALL 为数值，否则为 `null`

#### Scenario: 无 per_100g 热量记录

- **WHEN** 某 `food_item` 无可用 `per_100g` 热量数据
- **THEN** 该食材项 SHALL 仍包含 `foodName` 与 `defaultUnit`，且 `caloriesPer100g` SHALL 为 `null`

### Requirement: 同步与异步路径 SHALL 输出一致食材形状

`POST /api/ai/dashscope/vision/dish-ingredients` 成功响应中的 `data.recognition.ingredients`，与餐图异步识别 poll 成功时 `data.result.recognition.ingredients`，SHALL 遵循相同的食材元素字段集合与语义。

#### Scenario: 异步 poll 成功态

- **WHEN** 异步任务 `status` 为成功且 `result` 含 `recognition.ingredients`
- **THEN** 每条食材 SHALL 满足与本规范「匹配成功的食材项」相同的字段规则（在匹配成功的前提下）
