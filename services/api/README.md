# shiji-api

## DashScope 连通性（联调）

- **用途**：验证阿里云百炼 / DashScope 文本模型与 `DASHSCOPE_API_KEY` 是否配置正确；**非**产品功能接口。
- **环境**：设置环境变量 `DASHSCOPE_API_KEY`（或在本地使用 `application-local.yml` 覆盖，勿提交密钥）。默认模型见 `application.yml` 中 `dashscope.text-model`。
- **接口**：`POST /api/ai/dashscope/ping`（需 `Authorization: Bearer <token>`）
- **请求体**（可选）：`{ "message": "你好" }`；省略 `message` 时使用 `src/main/resources/prompts/dashscope/default-ping-message.txt` 中的默认短文本。
- **成功响应 `data`**：`reply`（模型原文）、`model`（本次使用的模型名）。
- **示例**（先登录拿到 token）：

```bash
curl -s -X POST "http://localhost:8080/api/ai/dashscope/ping" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{}"
```

## 菜品/食材视觉识别（MVP）

- **接口**：`POST /api/ai/dashscope/vision/dish-ingredients`（需 `Authorization: Bearer <token>`）
- **请求体**：`{ "imageBase64": "<base64或data:image/...;base64,...>" }`
- **默认模型**：`dashscope.vision.model=qwen3.6-flash`
- **阈值规则**：`dashscope.vision.confidence-threshold` 默认 `0.7`；`dish` 低于阈值或歧义时返回 `dish=null` 并带 `dishRejection`
- **错误码**：`NOT_FOOD_IMAGE`、`UNRECOGNIZABLE_IMAGE`、`MODEL_OUTPUT_INVALID`、`UPSTREAM_AI_ERROR`
- **说明**：数据库 BIGINT 在响应中按字符串返回（如 `dishId` / `ingredientId`）
- **`recognition.ingredients[]`**（词表命中时）：除 `ingredientId`、`confidence` 外，还包含 **`foodName`**、**`defaultUnit`**（来自 `food_item`）；**`caloriesPer100g`** 为 `food_nutrition` 中 `nutrient_basis=per_100g` 的最新版热量（无记录时为 `null`）。关联仍以 `ingredientId` 为准。

## 餐图异步识别任务（上传与识别解耦）

- **创建任务**：`POST /api/ai/meal-photo/recognitions`（需 `Authorization: Bearer <token>`）
- **请求体**：`{ "fileId": <long> }`（须为当前用户名下、`biz_type=meal_photo`、`status=uploaded` 的文件）
- **成功 `data`**：`taskId`（UUID 字符串）、`status`（初始多为 `pending`）
- **轮询**：`GET /api/ai/meal-photo/recognitions/{taskId}`
- **轮询 `data`**：`taskId`、`status`（`pending` | `processing` | `success` | `failed`）；成功时 `result` 与同步接口的 `DishIngredientVisionResponse` 形状一致（`requestId` + `recognition`，含 `ingredients[].foodName` / `defaultUnit` / `caloriesPer100g`）；失败时 `errorCode` / `errorMessage` 有值，`result` 为 null
- **SLA**：`recognition.task.timeout-seconds`（默认 60）内未终态则由服务端标记超时失败；`recognition.task.retention-days`（默认 7）后清理终态任务行
- **配置示例**（`application.yml`）：`recognition.task.timeout-seconds`、`recognition.task.retention-days`、`recognition.task.scan-delay-ms`（超时兜底扫描间隔）、`recognition.task.cleanup-cron`（清理调度）
- **错误码（节选）**：`RECOGNITION_FILE_INVALID`(31111)、`RECOGNITION_TASK_NOT_FOUND`(31108)、`RECOGNITION_TASK_TIMEOUT`(31109)、`RECOGNITION_FILE_READ_FAILED`(31110)；其余与视觉识别一致（如 `NOT_FOOD_IMAGE` 等由异步任务失败态返回）

```bash
curl -s -X POST "http://localhost:8080/api/ai/meal-photo/recognitions" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"fileId\": 123}"

curl -s "http://localhost:8080/api/ai/meal-photo/recognitions/<taskId>" \
  -H "Authorization: Bearer <token>"
```

## 饮食记录保存

- **接口**：`POST /api/meal-records`（需 `Authorization: Bearer <token>`）
- **`record_date`**：由请求中的 `recordedAt` 在 **`Asia/Shanghai`** 下解释为本地时刻后取日期写入；客户端勿依赖自行传入的日历日覆盖该结果。
- **总热量**：服务端按食物行 `estimatedCalories` 求和（`null` 计 0）写入 `meal_record.total_estimated_calories`。
- **图片**：仅允许引用已存在且 `user_id` 为当前用户、`status=uploaded` 的 `file_asset`。

## 标准食物搜索（分页）

- **接口**：`GET /api/food-items`（需 `Authorization: Bearer <token>`）
- **参数**：`q`（可选，名称/别名模糊）、`page`（从 0 开始）、`size`（默认 20，最大 50）
- **响应 `data`**：`items`（`id`, `foodName`, `caloriesPer100g`）、`hasNext`、`page`、`size`
