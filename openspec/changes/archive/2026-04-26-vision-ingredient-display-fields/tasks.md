## 1. 后端 DTO 与词表/营养

- [x] 1.1 扩展 `DishIngredientVisionResponse.Ingredient`：新增 `foodName`、`defaultUnit`、`caloriesPer100g`（Double 或 BigDecimal，JSON 数字或 null）
- [x] 1.2 在视觉识别路径批量解析 `food_nutrition`（per_100g 热量），避免 N+1；无数据时 `caloriesPer100g` 为 null
- [x] 1.3 更新 `DishIngredientVisionService.matchIngredient`（或等价）在匹配成功时填入上述字段

## 2. 测试与文档

- [x] 2.1 更新 `DishIngredientVisionIntegrationTest`（及必要时 `MealPhotoRecognitionTaskIntegrationTest`）：断言 `foodName` / `defaultUnit`；有营养种子时断言 `caloriesPer100g`
- [x] 2.2 更新 `services/api/README.md`：视觉与异步识别章节说明 `ingredients[]` 新增字段

## 3. Flutter 客户端

- [x] 3.1 扩展 `meal_photo_recognition_models.dart` 中 `VisionIngredient`（及同步视觉若已建模）
- [x] 3.2 记录页应用识别结果时优先使用 `foodName`、`caloriesPer100g`、`defaultUnit` 构建 `DraftFoodItem`（替代纯占位「食物 #id」）
