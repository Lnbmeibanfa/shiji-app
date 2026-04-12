# shiji-api

## 饮食记录保存

- **接口**：`POST /api/meal-records`（需 `Authorization: Bearer <token>`）
- **`record_date`**：由请求中的 `recordedAt` 在 **`Asia/Shanghai`** 下解释为本地时刻后取日期写入；客户端勿依赖自行传入的日历日覆盖该结果。
- **总热量**：服务端按食物行 `estimatedCalories` 求和（`null` 计 0）写入 `meal_record.total_estimated_calories`。
- **图片**：仅允许引用已存在且 `user_id` 为当前用户、`status=uploaded` 的 `file_asset`。

## 标准食物搜索（分页）

- **接口**：`GET /api/food-items`（需 `Authorization: Bearer <token>`）
- **参数**：`q`（可选，名称/别名模糊）、`page`（从 0 开始）、`size`（默认 20，最大 50）
- **响应 `data`**：`items`（`id`, `foodName`, `caloriesPer100g`）、`hasNext`、`page`、`size`
