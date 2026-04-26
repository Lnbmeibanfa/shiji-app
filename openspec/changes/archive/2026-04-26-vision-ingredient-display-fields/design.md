## Context

- `DishIngredientVisionService` 在 `matchIngredient` 中已从 `FoodItemEntity` 得到 `id`、`foodName`、`defaultUnit` 等，但 `DishIngredientVisionResponse.Ingredient` 仅序列化 `ingredientId` 与 `confidence`。
- 每 100g 热量不在 `food_item` 表本行，而在 `food_nutrition`（如 `per_100g` + `calories`）；词表 `DishIngredientVocabProvider.Snapshot` 当前未加载营养。

## Goals / Non-Goals

**Goals:**

- 在 **`ingredients[]`** 上增加前端可直接用于列表回显的只读字段：`foodName`、`defaultUnit`、`caloriesPer100g`（可为 null）。
- 同步视觉接口与异步 poll 的 `result` 使用**同一 DTO**，行为一致。
- 保持 **`ingredientId` 为关联标准食物的主键**；展示字段为解析时刻快照，库内后续改名不反向改写已存 JSON。

**Non-Goals:**

- 不在本变更中扩展 **`dish`** 的展示名字段（用户本次仅提食材）；若后续需要可单独变更。
- 不改变模型 prompt、阈值或词表匹配算法。
- 不要求对所有历史任务 JSON 做回填迁移。

## Decisions

1. **DTO 扩展方式**  
   - 在 `Ingredient` record 上**新增可选/可空字段**（`foodName`、`defaultUnit`、`caloriesPer100g`），保留原有字段。  
   - **理由**：向后兼容、Jackson 反序列化旧客户端忽略未知字段无影响。

2. **`caloriesPer100g` 来源**  
   - 在识别请求路径内：对**本轮匹配到的** `food_item` id 集合，**批量查询** `food_nutrition` 中 `nutrient_basis=per_100g` 的最新或约定版本热量（与现有 `FoodNutritionRepository` 语义对齐），构建 `Map<Long, BigDecimal>`（或 Double）供映射使用。  
   - 无营养行时 **`caloriesPer100g` 为 null**。  
   - **备选**：仅在 `Ingredient` 放 `foodName`+`defaultUnit`，热量由客户端再调搜索接口；**未选**，因用户明确要求热量回显且 N 次匹配食材数量有限。

3. **实现落点**  
   - 优先在 **`DishIngredientVocabProvider` 或独立小组件** 中扩展 Snapshot / 一次批量加载营养，避免在 `matchIngredient` 内逐条查库（N+1）。  
   - `matchIngredient` 签名可改为接收 `FoodItemEntity` + 可选热量，或从上下文取 Map。

4. **Flutter**  
   - 扩展 `VisionIngredient` 与 `DraftFoodItem.aiFromIngredientId` 的调用方：优先使用响应中的 `foodName`、`caloriesPer100g` 构建草稿行。

## Risks / Trade-offs

- **[营养数据缺失]** → 前端必须处理 `caloriesPer100g == null`，仍可展示名称与默认单位。  
- **[快照与库不一致]** → 文档与 spec 标明为展示快照；保存请求仍以用户编辑与 `foodItemId` 为准。  
- **[批量营养查询成本]** → 单次识别食材条数上限已有（如 10），批量查询可接受。

## Migration Plan

- 部署顺序：先后端与契约，再发版客户端；旧客户端忽略新字段不受影响。  
- 回滚：还原 DTO 与映射（新客户端已依赖新字段时需同步回滚客户端）。

## Open Questions

- `defaultUnit` 是否足够，或是否需要额外 `foodCode`（运维/调试）；当前按产品回显需求仅 `foodName`+热量+单位。
