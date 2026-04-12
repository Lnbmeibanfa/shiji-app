## Context

- 数据模型已包含：`meal_record`（一餐主表）、`meal_food_item`（食物行）、`meal_record_image`（餐与 `file_asset` 关联）、`meal_record_emotion_rel`（餐与 `emotion_tag` 关联），以及字典表 `food_item`、`food_nutrition`、`emotion_tag`。
- 文件上传已由 `POST /api/files/upload` 完成，`file_asset` 含 `user_id`（见 `openspec/specs/file-upload/spec.md`）。
- 统一响应为 `ApiResponse`（见 `openspec/specs/api-http-contract/spec.md`）。

## Goals / Non-Goals

**Goals:**

- 定义保存一餐的**单接口**语义：认证、校验、单事务落库、与不变量一致。
- 固定 **`record_date` 推导规则**、**总热量求和规则**、**情绪仅写关联表 + 主情绪展示顺序**、**文件归属校验**。

**Non-Goals:**

- AI 识别流水线、异步识别任务、食物库自动建项（本变更仅规范保存已给出的结构化结果）。
- 客户端 UI、Flutter 路由与 multipart（已有独立变更覆盖上传通路）。
- 列表/日历查询接口、编辑/删除餐次（可后续单独变更）。

## Decisions

### 1. 情绪：只写 `meal_record_emotion_rel`

- **决策**：保存请求的「情绪」部分**只插入/更新** `meal_record_emotion_rel`（及必要的 `emotion_tag` 外键解析），**不在请求体中要求**写入 `meal_record.primary_emotion_code`。
- **主情绪展示**：持久化完成后，「主情绪」定义为：在同一 `meal_record_id` 下，按 `emotion_tag.sort_order` **升序**，再按 `emotion_tag.id` **升序**取**第一条**关联记录对应的情绪（若需展示编码，则取该行的 `emotion_tag.emotion_code`）。与「用户点选顺序」冲突时，以本排序为权威，除非未来在关联表增加显式 `sort_order` 字段并改规范。
- **`primary_emotion_code` 列**：若主表仍保留该列，实现 **MAY** 在同一事务内、插入关联行之后，用上述「第一条」规则回填该列，以便旧查询或索引；**MUST NOT** 接受客户端随意写入与关联表不一致的主情绪编码。

### 2. `record_date` 由 `recorded_at` 推导

- **决策**：服务端根据 `meal_record.recorded_at`（datetime）在**固定时区**下计算 `record_date`（date）。
- **默认**：使用 `Asia/Shanghai`（与常见国内产品一致）；若项目已有全局 `ZoneId` 配置，**MUST** 与该配置一致并在实现中单一引用。
- **规则**：`record_date = recorded_at.atZone(固定时区).toLocalDate()`（或等价实现）。客户端**可以**不传 `record_date`；若传入，服务端 **MUST** 以推导结果为准覆盖/忽略客户端值，避免跨时区篡改。

### 3. 总热量（及宏量可选一致策略）

- **决策**：`meal_record.total_estimated_calories` **MUST** 等于本次保存请求中**所有 `meal_food_item` 行**的 `estimated_calories` 之和。
- **空值**：对「求和」而言，`estimated_calories` 为 `NULL` 的行 **MUST** 计为 **0**（与「识别行存在但热量未估算」一致）；若业务上不允许无热量行，可在后续版本加严校验，本变更以可落地为先。
- **其它宏量**（蛋白质/脂肪/碳水）：**SHOULD** 与各行同名字段之和一致，由服务端在同一事务内重算写入 `meal_record`，避免客户端算术错误。

### 4. 图片 `file_id` 校验

- **决策**：对每个 `meal_record_image` 候选 `file_id`：
  - **MUST** 存在 `file_asset.id == file_id`；
  - **MUST** 满足 `file_asset.user_id ==` 当前认证用户 `user_id`。
- **拒绝**：不存在、属他人、`status` 非可用上传结果（若表中有状态字段，实现 **SHOULD** 仅允许 `uploaded` 等成功状态）时，**MUST** 返回可展示业务错误，且 **MUST NOT** 部分落库餐次（整单事务回滚）。

### 5. API 形态（建议，细节以 spec 为准）

- **方法/路径**：`POST /api/meal-records`（或项目统一资源前缀下的等价路径），**仅认证用户**。
- **事务**：插入 `meal_record` 与子表 **MUST** 在同一事务中提交或全部回滚。
- **幂等**：首版不强制幂等键；重复提交产生多条记录为可接受行为，除非产品后续要求 `Idempotency-Key`。

## Risks / Trade-offs

- **[Risk] 主情绪排序与用户心理顺序不一致** → 若产品强依赖「先选为主」，需在 `meal_record_emotion_rel` 增加 `sort_order` 或强制 `is_primary` 语义，并更新本 design/spec。
- **[Risk] 时区与 `recorded_at` 语义** → 若客户端传 UTC 而展示按本地日，**MUST** 在 API 文档中说明 `recorded_at` 的时区含义；当前按服务端固定时区映射为 `record_date`。
- **[Trade-off] 服务端重算总热量** → 客户端传入的整餐 totals 若与行和不一致将被覆盖，减少作弊与错误，但调试时需注意。

## Migration Plan

- 新接口增量发布；无数据迁移。
- **回滚**：下线 Controller 路由即可；已写入数据保留。

## Open Questions

- `meal_record_emotion_rel.is_primary` 是否与「排序取第一条」并存：若并存，**主情绪**是否改为「`is_primary=1` 优先，否则按排序」？当前用户决策为「查第一个」，故首版以排序为准；若表中已有 `is_primary`，实现可约定写入时仅一条 `is_primary=1` 并与排序一致。
