## Why

「记录饮食」页需要完整闭环：从选图/识别、手动搜食物、列表编辑到保存。当前缺少**标准食物分页搜索 API**与**按状态机串联的前端流程**；真实 AI 识别尚未接入。需要先用**后端食物检索 + 前端状态机 + 上传后假数据跑通保存**，再替换为真实识别服务。

## What Changes

- 新增 **HTTP 接口**：按名称**模糊**查询 `food_item`，**分页**返回（含每 100g 热量展示所需字段），供「添加食物」底栏使用。
- 新增 **Flutter 组件**：与设计稿一致的「添加食物」底部弹层（搜索框 + 列表 + 触底加载下一页）。
- 在 **`RecordMealPage`（或等价页面）** 实现 **页面级状态机**：上传中 / AI 识别中（可打断）/ 可编辑；**上传或识别进行中禁止保存**，保存按钮置灰。
- **冲突策略**：AI 识别结果**直接追加**到食物列表；行级用数据库已有字段区分来源（`meal_food_item.recognition_source`：`ai` 与 `user_manual` 等）；整餐 `record_method` 使用 `meal_record.record_method` 枚举（`manual` | `photo` | `photo_ai` | `quick_add`），与既有表结构一致。
- **占位**：用户上传图片成功后，**前端注入固定几条「AI」食物**（绑定真实 `food_item_id` 或约定快照），模拟识别完成，直至真实 AI 可用。
- 打通 **保存**：至少一项食物、`record_method` / 行级 `recognition_source` 与后端 `POST /api/meal-records` 契约一致。

## Capabilities

### New Capabilities

- `food-item-search-api`：标准食物分页模糊检索 API（查询参数、分页语义、响应字段、仅可用食物、与 `food_nutrition` 的 per-100g 热量展示约定）。
- `record-meal-ai-flow`：记录饮食页状态机、添加食物底栏组件、上传后 AI 占位数据、保存闸门与 `record_method` / 行级来源字段的映射规则。

### Modified Capabilities

- （无）本变更通过新增接口与移动端行为完成；若归档时需合并到根 `openspec/specs/`，再单独走 archive 流程。

## Impact

- **后端**（`services/api`）：新 Controller/Service/Repository 或查询；可能新增 DTO；`FoodItemRepository` 需支持模糊+分页；若尚无 `food_nutrition` 的 JPA 映射，可用 `@Query`/原生 SQL 联结。
- **前端**（`apps/mobile`）：`record_meal` 特性内状态与 UI；调用新 GET 接口；`CreateMealRecordRequest` 组装；可选轻量本地模型字段区分 AI 行展示。
- **依赖**：已存在的认证、`POST /api/meal-records`、文件上传链路；**不**在本变更中实现真实 AI 识别服务。
