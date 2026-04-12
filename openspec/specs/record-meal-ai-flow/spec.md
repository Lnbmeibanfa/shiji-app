# 记录饮食 AI 流程与添加食物 UI（record-meal-ai-flow）

本规范定义 Flutter 端「记录饮食」页的状态机、添加食物底部弹层、AI 占位与保存闸门行为。

---

## 规范要求

### Requirement: 页面状态机与保存闸门

记录饮食页 MUST 维护至少以下逻辑状态：`idle`（可编辑）、`uploading`（图片上传中）、`aiRecognizing`（AI 识别进行中，含占位模拟）。在 `uploading` 或 `aiRecognizing` 期间，用户 MUST NOT 能提交保存；保存主按钮 MUST 置灰（`onPressed` 为空或等价禁用样式）。**可保存**当且仅当：食物列表非空，且当前非 `uploading`，且非 `aiRecognizing`。

#### Scenario: 识别中保存被禁止

- **WHEN** 当前为 `aiRecognizing`
- **THEN** 保存按钮为禁用态，且即使用户有食物项，保存操作 MUST NOT 提交

#### Scenario: 无食物时保存被禁止

- **WHEN** 食物列表为空，且处于 `idle`
- **THEN** 保存按钮为禁用态

### Requirement: AI 识别态可打断

在 `aiRecognizing` 期间，界面 MUST 展示阻塞性 Loading（或遮罩），且 MUST 提供「手动打断」入口。用户触发打断后，状态 MUST 变为 `idle`，且 MUST NOT 自动追加本次 AI 识别的占位结果（若尚未完成）。

#### Scenario: 打断后不追加 stub 行

- **WHEN** 用户在 stub 识别完成前触发打断
- **THEN** 食物列表中 MUST NOT 增加本次识别任务拟追加的项

### Requirement: AI 结果直接追加且可区分来源

当 AI 识别成功（stub 或真实）时，系统 MUST 将识别出的食物**追加**到当前列表末尾，**不得**弹出覆盖确认或合并策略。每一行 MUST 在 UI 上可标注为「AI 识别」。持久化时，对应 `MealFoodItemRequest.recognitionSource` MUST 为 `ai`；用户通过搜索添加的行 MUST 为 `user_manual`（或与后端 `meal_food_item` 注释一致的枚举值）。

#### Scenario: 手动与 AI 行共存

- **WHEN** 用户先手动搜索添加「米饭」，再完成 AI 追加「鸡胸肉」
- **THEN** 列表中 MUST 存在两行，且各行 `recognitionSource` 分别反映手动与 AI

### Requirement: 餐别默认按左闭右开时间区间

进入页面时，默认餐别 MUST 由当前时间按以下左闭右开区间推导：早餐 `[05:00, 10:00)`，午餐 `[10:00, 15:00)`，晚餐 `[15:00, 20:00)`，夜宵 `[20:00, 次日 05:00)`。

#### Scenario: 午夜属于夜宵

- **WHEN** 本地时间为 02:00
- **THEN** 默认餐别 MUST 为夜宵区间

### Requirement: 餐级 record_method 与占位 AI

保存请求 MUST 设置 `recordMethod` 与 `meal_record.record_method` 枚举一致。本变更中：若用户走了「上传图片 + AI 占位路径」且成功完成识别并保存，MUST 使用 `photo_ai`；若仅手动搜索添加、无有效照片流程，MUST 使用 `manual`；若仅有照片上传无 AI 步骤，MUST 使用 `photo`（与产品约定一致）。Stub 阶段 MUST 在「上传成功 + 识别完成」后使用 `photo_ai`。

#### Scenario: 仅手动添加保存为 manual

- **WHEN** 用户未使用上传图片或识别流程，仅添加食物并保存
- **THEN** 请求体 `recordMethod` MUST 为 `manual`

### Requirement: 添加食物底部弹层组件

系统 MUST 提供与设计稿一致的「添加食物」底部弹层：标题、关闭、圆角搜索框（占位「搜索食物...」）、下方列表项左名称右「kcal/100g」。用户点选某一行后，MUST 关闭弹层并回调所选食物（含 `foodItemId`、展示名、每 100g 热量），用于向父列表追加一行。

#### Scenario: 选中后关闭并回调

- **WHEN** 用户点击列表中某食物行
- **THEN** 弹层 MUST 关闭，且父级 MUST 收到该食物数据

### Requirement: 搜索与分页由后端驱动且触底加载

弹层内列表 MUST 调用标准食物搜索 API。搜索词 MUST 防抖后重置为第一页并重新请求。用户滚动至列表底部且仍有下一页时，MUST 自动请求下一页并**追加**到当前结果列表。

#### Scenario: 搜索词变更重置分页

- **WHEN** 用户将搜索框从「a」改为「ab」
- **THEN** 列表 MUST 从第一页重新加载，**不得**仅追加旧 `q` 的下一页

### Requirement: 上传图片后使用 stub 跑通识别流程

在真实 AI 未接入前，当用户上传图片并成功得到 `file_id` 后，应用 MUST 进入 `aiRecognizing`，并在短延迟后**模拟完成**，向列表追加固定数量的食物行（使用种子数据中存在的 `food_item_id`），以跑通 `uploading → aiRecognizing → idle → 保存` 全流程。

#### Scenario: Stub 完成后可保存

- **WHEN** stub 识别完成且列表至少有一行
- **THEN** 保存按钮 MUST 可点击，且 `recordMethod` 符合 `photo_ai` 约定
