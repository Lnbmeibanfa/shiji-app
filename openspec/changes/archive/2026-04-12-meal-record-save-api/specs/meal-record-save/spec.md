# 饮食记录保存 API（meal-record-save）

本规范定义「保存一餐」后端接口的行为约束，与 `openspec/changes/meal-record-save-api/design.md` 中的技术决策一致。

---

## ADDED Requirements

### Requirement: 已认证用户可创建一餐记录

系统 MUST 提供保存饮食记录的 HTTP 接口（实现阶段路径建议为 `POST /api/meal-records`，以代码与路由配置为准），**仅接受已认证用户**调用。成功时响应 MUST 使用 `ApiResponse` 封装，且 `code` MUST 为 0；`data` MUST 至少包含新建 `meal_record` 的持久化标识（如 `mealRecordId`）及必要展示字段（实现可扩展）。

#### Scenario: 未登录被拒绝

- **WHEN** 请求未携带有效会话令牌
- **THEN** HTTP 401，且响应体符合项目统一未授权 JSON 约定

#### Scenario: 成功创建

- **WHEN** 已登录用户提交合法请求体且校验通过
- **THEN** HTTP 200，`ApiResponse.code` 为 0，数据库中插入一条 `meal_record` 及请求约定的子表数据，且为**同一事务**提交

### Requirement: record_date 必须由 recorded_at 推导写入

系统 MUST 从请求中的 `recorded_at` 推导 `record_date` 并写入 `meal_record.record_date`。推导 MUST 使用项目约定的单一固定时区（默认 `Asia/Shanghai`，若全局配置不同则以配置为准）。客户端若传入 `record_date`，服务端 MUST 以推导结果为准，不得采用与 `recorded_at` 矛盾的客户端日期。

#### Scenario: 按用餐时间落到日历日

- **WHEN** `recorded_at` 为某合法日期时间
- **THEN** `record_date` MUST 等于该时间在约定时区下的日历日期

### Requirement: 整餐总热量等于食物行热量之和

系统 MUST 将 `meal_record.total_estimated_calories` 设为本次请求中所有 `meal_food_item` 行的 `estimated_calories` 之和。对求和而言，单行的 `estimated_calories` 为 `NULL` 时 MUST 按 0 计入。

系统 SHOULD 以同样方式将 `meal_record` 上其它宏量字段（`total_estimated_protein`、`total_estimated_fat`、`total_estimated_carb`）设为对应行字段之和，`NULL` 按 0 计。

#### Scenario: 多行求和

- **WHEN** 请求包含三行食物，估算热量分别为 100、NULL、200
- **THEN** 持久化后的 `meal_record.total_estimated_calories` MUST 为 300

### Requirement: 情绪仅写入关联表且主情绪由查询确定

保存接口 MUST NOT 依赖客户端写入 `meal_record.primary_emotion_code` 作为情绪真源。情绪数据 MUST 仅通过 `meal_record_emotion_rel`（引用 `emotion_tag`）持久化。

定义「主情绪」查询规则如下：对给定 `meal_record_id`，在关联的 `emotion_tag` 上按 `sort_order` 升序、`emotion_tag.id` 升序取第一条关联记录；展示用主情绪编码 MUST 来自该条目的 `emotion_tag.emotion_code`。若实现选择在同一事务内回填 `meal_record.primary_emotion_code`，则 MUST 与该规则一致。

#### Scenario: 多条情绪时的主情绪

- **WHEN** 同一餐关联两条情绪标签，且二者 `sort_order` 分别为 2 与 1
- **THEN** 「主情绪」MUST 对应 `sort_order` 较小（即 1）的标签

### Requirement: 图片 file_id 必须存在且属于当前用户

当请求包含与图片相关的 `file_id` 时，对每个 `file_id`，系统 MUST 校验：

1. 存在 `file_asset` 记录其主键等于该 `file_id`；
2. 该记录的 `user_id` MUST 等于当前认证用户的 `user_id`。

若任一校验失败，系统 MUST 拒绝整笔保存请求（不得部分写入餐次），并 MUST 通过 `ApiResponse` 返回非 0 `code` 与可读 `message`。

#### Scenario: 引用不存在的文件

- **WHEN** 请求包含数据库中不存在的 `file_id`
- **THEN** 请求失败，`code` 非 0，且不插入 `meal_record`

#### Scenario: 引用他人文件

- **WHEN** `file_id` 存在但 `file_asset.user_id` 与当前用户不一致
- **THEN** 请求失败，`code` 非 0，且不插入 `meal_record`

### Requirement: 业务错误与统一响应

因业务规则（文件校验失败、情绪标签不存在或停用、必填字段缺失等）导致的失败，系统 MUST 返回 `ApiResponse` 形状，且 `code` MUST 为非 0；**MUST NOT** 向客户端暴露内部堆栈。模块 **SHOULD** 使用实现 `ErrorCode` 的枚举承载错误码，以符合 `api-http-contract`。

#### Scenario: 情绪标签非法

- **WHEN** 请求引用不存在的 `emotion_tag_id` 或规范要求停用的标签
- **THEN** 请求失败，返回统一错误外壳与可读说明
