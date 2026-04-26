# 受控词表单图菜品/食材识别（dish-ingredient-vision）

本规范定义后端同步 API：单张菜品照片在**全局** `dish` / `dish_alias` / `food_item` 词表约束下的识别行为、JSON 契约、置信度规则与错误码。实现 MUST 符合 `openspec/specs/ai-capability`（后端统一调用、prompt 文件化、输出安全处理）。

---

## ADDED Requirements

### Requirement: 同步识别 API 接受单图并返回结构化 JSON

系统 SHALL 提供同步 HTTP API，接受**单张**食物/菜品相关图片，返回本规范定义的 **`recognition` 根对象**（见下文物档化字段），且响应体 SHALL 包含 **`request_id`**（由服务端生成，用于链路追踪）。

系统 SHALL 在调用多模态模型前对图像做**保守预处理**（等比缩放、必要时压缩），参数 SHALL 可配置，且 SHALL 以满足识别质量为约束（避免过度有损压缩导致细节丢失）。

#### Scenario: 成功返回可解析的结构化结果

- **WHEN** 客户端提交合法图片且模型返回符合 schema 的 JSON，且服务端校验通过
- **THEN** 系统 SHALL 返回 HTTP 200，且 body SHALL 包含 `request_id` 与 `recognition` 对象

#### Scenario: 响应携带追踪字段

- **WHEN** 任意一次识别请求完成（成功或业务错误）
- **THEN** 系统 SHALL 在响应中带上本次 `request_id`，且 MAY 在 `recognition.model_meta` 中记录模型名、prompt 版本、处理后图像尺寸等审计信息

---

### Requirement: 模型输出必须为严格 JSON 且 prompt 约束边界

系统 SHALL 通过版本化 prompt 文件要求多模态模型**仅输出一个 JSON 对象**，SHALL NOT 包含 Markdown 围栏、前后缀说明文字。

Prompt SHALL 明确边界：

- 若用户上传内容**明显不是食物/菜品**（如风景、人像、纯文字截图与菜品无关等），模型 SHALL 在 JSON 内设置约定字段（例如 `outcome: "NOT_FOOD"`），SHALL NOT 编造菜名或食材。
- 若图像模糊或无法从可见内容识别任何词表内菜品或食材，模型 SHALL 设置约定字段（例如 `outcome: "UNRECOGNIZABLE"`）。

系统 SHALL 对模型原始文本做 JSON 解析；若解析失败或根类型非 object，SHALL 按本规范「模型输出非法」类错误处理（见错误码要求）。

#### Scenario: 解析失败触发错误

- **WHEN** 模型输出无法解析为 JSON 或缺少规范必填字段
- **THEN** 系统 SHALL 返回非 200 的 HTTP 状态或业务包装中的明确错误码（由项目 HTTP 契约统一约定），且 SHALL NOT 将不可信自由文本当作业务结果透传

---

### Requirement: 菜品命中规则（唯一候选 + 置信度阈值 + 别名映射）

系统 SHALL 以 **τ = 0.7** 为默认阈值（可配置），且本阶段 **τ_dish = τ_ingredient = 0.7**。

`dish` 字段合法，当且仅当：

1. 模型给出**唯一**菜品语义候选（规范为：模型输出中菜品相关字段表示**单一**命中，不得为并列多个同等 `dish`）；且
2. 模型自报该菜品 `confidence ≥ τ_dish`；且
3. 服务端将模型返回的**别名字符串或标准名**成功映射到库内 `dish_id`（经 `dish` 或 `dish_alias` 查表）。

若 **confidence < τ_dish** 或无法唯一映射或无法映射到库内 id，系统 SHALL 设置 `dish` 为 `null`，SHALL NOT 为凑结果而写入未校验的 `dish_id`。

若多条 `dish_alias` 记录与模型字符串匹配且解析到**同一** `dish_id`，系统 SHALL 在 `match.alias_id` 中选取**确定性第一条**（例如按别名表主键升序的最小 id）。

若模型输出导致**多 `dish` 并列**或语义歧义（无法唯一），系统 SHALL 设置 `dish` 为 `null`，且 `dish_rejection.code` SHALL 为 `AMBIGUOUS`。

#### Scenario: 高置信度且唯一映射时返回 dish

- **WHEN** 模型返回唯一菜品字符串且 `confidence = 0.85`，且服务端映射到 `dish_id = "1001"`
- **THEN** `recognition.dish` SHALL 非空，且 `dish.dish_id` SHALL 为 `"1001"`，且 `dish.confidence` SHALL 为 `0.85`

#### Scenario: 低置信度不强行给菜名

- **WHEN** 模型返回某菜品字符串但 `confidence = 0.55`
- **THEN** `recognition.dish` SHALL 为 `null`，且 SHALL 进入食材识别路径（若模型仍返回食材且满足阈值），且 `dish_rejection` SHALL 说明原因（如 `LOW_CONFIDENCE`）

---

### Requirement: 未命中菜品时的食材回退与上限

当 `dish` 为 `null` 时，系统 SHALL 仅接受映射到 **`food_item` 主键**的食材项；模型 SHALL 为每项给出 `confidence`，且 SHALL 丢弃 `confidence < τ_ingredient` 的项。

系统 SHALL 对保留项按 `confidence` 降序排序后**截断至最多 10 条**。

当 `dish` 非 `null` 时，系统 MAY 同时接受模型返回的食材列表；服务端 MAY 按业务规则合并或忽略（本规范不强制互斥，但每项仍 MUST 满足映射与置信度规则）。

#### Scenario: 食材超过 10 条时截断

- **WHEN** 模型返回 15 条食材且均 ≥ τ
- **THEN** 响应中 `ingredients` SHALL 至多包含 10 条

---

### Requirement: 拒识原因对调用方可见

当 `dish` 为 `null` 且本次请求意图包含菜品识别时，系统 SHALL 在 `recognition.dish_rejection` 中给出机器可读 `code`，且 MAY 给出 `detail` 文本（面向开发者，非用户文案）。

建议 `code` 集合（可实现扩展，但 MUST 文档化）：

- `LOW_CONFIDENCE`
- `NO_CATALOG_MATCH`
- `AMBIGUOUS`
- （当模型声明非食物）`NOT_FOOD`
- （当模型声明无法识别）`UNRECOGNIZABLE`

#### Scenario: 非食物图返回明确错误

- **WHEN** 模型按 prompt 约定声明 `NOT_FOOD`
- **THEN** 系统 SHALL 返回带有约定业务错误码的响应（HTTP 层可为 4xx 或由全局错误包装映射），且 body SHALL 可区分于「成功但无命中」

---

### Requirement: 标识符在 JSON 中的表示

所有来自数据库的 BIGINT 主键（`dish_id`、`alias_id`、`ingredient_id`）在 API JSON 中 SHALL 以**十进制字符串**表示（例如 `"1001"`），SHALL NOT 依赖 IEEE 754 双精度整数精度。

#### Scenario: id 以字符串承载

- **WHEN** `dish_id` 数据库值为 9007199254740993
- **THEN** JSON 中 SHALL 仍为精确字符串 `"9007199254740993"`

---

### Requirement: recognition 对象字段契约（规范性）

`recognition` 对象 SHALL 包含：

- `schema_version`：固定为 `"1.0"`（本规范版本）
- `dish`：`null` 或对象，包含 `dish_id`（string）、`confidence`（number 0–1）、`match`（对象：`via` 为 `dish` 或 `alias`；若 `via` 为 `alias` 则 MUST 含 `alias_id` string）
- `ingredients`：`array`，元素为 `{ "ingredient_id": string, "confidence": number }`
- `dish_rejection`：当 `dish` 为 `null` 时 SHOULD 存在；当 `dish` 非 `null` 时 MAY 省略
- `model_meta`：可选对象（模型名、prompt 版本、处理后宽高、原图 hash 等）

根响应 SHALL 包含 `request_id`（string），与 `recognition` 并列或按项目统一 envelope 包装（若项目已有标准 envelope，SHALL 在 tasks 实现阶段与 `api-http-contract` 对齐）。

#### Scenario: dish 与 ingredients 可并存

- **WHEN** 模型同时返回合法 `dish` 与若干食材
- **THEN** 响应 MAY 同时包含非空 `dish` 与非空 `ingredients`，且各自 MUST 独立满足映射与阈值规则

---

### Requirement: 错误码与 HTTP 语义

系统 SHALL 为至少以下情况提供**稳定可测**的业务错误码（具体 HTTP 状态码由项目统一错误处理约定，但 MUST 在 API 文档中写死映射）：

- `NOT_FOOD_IMAGE`：上传内容经模型判定非食物/非菜品场景
- `UNRECOGNIZABLE_IMAGE`：无法从图像识别词表内结果
- `MODEL_OUTPUT_INVALID`：输出非 JSON 或 schema 校验失败（可含重试策略说明）
- `UPSTREAM_AI_ERROR`：上游 DashScope 调用失败或超时

#### Scenario: 上游失败不伪装成功

- **WHEN** DashScope 返回错误或可判定超时
- **THEN** 系统 SHALL 返回 `UPSTREAM_AI_ERROR`（或等价命名），且 SHALL NOT 返回伪造的 `dish` / `ingredients`
