## Why

饮食记录相关的数据表（`meal_record`、`meal_food_item`、`meal_record_image`、`meal_record_emotion_rel` 等）已就绪，客户端在拍照上传拿到 `fileId` 之后，需要一条**已认证、可审计、与数据模型一致**的后端保存能力，将一餐信息原子落库。若无统一规范，容易出现情绪与主展示字段双写不一致、`record_date` 缺失、总热量与行项目不符、以及引用他人或未落库文件等安全问题。

## What Changes

- 新增**保存饮食记录**的 HTTP API（路径、请求体、校验规则、错误语义在 spec 中固定）。
- 明确业务不变量：**情绪仅写入关联表**；**`record_date` 由 `recorded_at` 推导**；**整餐总热量由食物行估算热量求和**；**图片 `file_id` 必须存在且属于当前用户**。
- 明确「主情绪」的展示语义：持久化后通过查询关联数据按约定排序取**第一条**，不依赖客户端单独维护主表上的冗余情绪编码（若表中仍有 `primary_emotion_code` 列，实现可选择在同一事务内按相同规则回填以优化列表，见 design）。

## Capabilities

### New Capabilities

- `meal-record-save`：定义保存一餐记录的后端行为（认证、事务边界、字段推导、文件校验、错误码与 `ApiResponse` 契约）。

### Modified Capabilities

- （无）本变更不修改既有 `file-upload` / `api-http-contract` 的规范条文；保存接口在**新能力**中引用其对 `ApiResponse`、认证的要求。

## Impact

- **后端**：新增 `meal`（或 `meal_record`）模块下的 Controller、Service、DTO、校验与持久化；可能新增 `ErrorCode`；`FileAssetRepository` 需支持按 `id` 与 `userId` 校验归属。
- **客户端**：后续在完整记录流中调用该接口；请求体需携带 `recorded_at`、可选食物行、可选图片 `fileId`、可选情绪标签引用等（以 spec 为准）。
- **数据库**：使用已有 DDL；不要求本变更内改表，除非实现选择去掉/废弃 `primary_emotion_code` 的写入路径（仍可与现有列共存）。
