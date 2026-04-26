# record-meal-ai-flow（delta）

本文件为 `openspec/specs/record-meal-ai-flow/spec.md` 的增量变更，用于对齐**上传与异步识别解耦**、**轮询期间不阻塞保存**、**真实识别结果可弹窗确认应用**等行为。

---

## MODIFIED Requirements

### Requirement: 页面状态机与保存闸门

记录饮食页 MUST 维护至少以下逻辑状态：`idle`（可编辑）、`uploading`（图片上传中）、`aiRecognizing`（AI 异步识别任务进行中，含轮询等待与 Stub 模拟）。在 `uploading` 期间，用户 MUST NOT 能提交保存；保存主按钮 MUST 置灰（`onPressed` 为空或等价禁用样式）。在 **真实异步识别轮询** 场景下，`aiRecognizing` **SHALL NOT** 作为禁止保存的独立条件：用户 MAY 在识别仍在进行时继续编辑并在满足下列条件时保存。**可保存**当且仅当：食物列表非空，且当前非 `uploading`。（Stub 开发阶段若仍采用「识别中禁止保存」的占位行为，MAY 通过本地开关与真实路径区分，但 SHALL NOT 与真实异步产品语义冲突。）

#### Scenario: 异步轮询中保存不被识别状态单独阻止

- **WHEN** 当前为 `aiRecognizing` 且仅因异步识别任务轮询等待、非 `uploading`，且食物列表非空
- **THEN** 保存按钮 SHALL NOT 仅因 `aiRecognizing` 而被禁用（除非产品另有显式规则）

#### Scenario: 上传中保存仍被禁止

- **WHEN** 当前为 `uploading`
- **THEN** 保存操作 MUST NOT 提交

#### Scenario: 无食物时保存被禁止

- **WHEN** 食物列表为空，且处于 `idle`
- **THEN** 保存按钮为禁用态

### Requirement: AI 识别态可打断

在 `aiRecognizing` 期间，界面 MUST 提供可感知的进行态反馈（MAY 为非全屏阻塞样式，例如顶部条、角标或与上传区并列提示）。界面 MUST 提供「取消/忽略本次识别任务」入口：用户触发后，客户端 SHALL 停止轮询（及可选通知后端取消，若后端提供取消接口则遵循其契约），且 MUST NOT 在尚未由用户确认应用前自动将本次识别结果追加到食物列表。

#### Scenario: 用户取消轮询不自动追加结果

- **WHEN** 用户在识别成功回调到达前主动取消/忽略本次任务
- **THEN** 食物列表中 MUST NOT 因本次任务自动增加行

### Requirement: AI 结果直接追加且可区分来源

当 AI 识别成功时，系统 MUST 将拟展示的食物行以可区分来源的方式呈现。对于 **真实异步识别** 路径，系统 SHALL 允许通过弹窗等形式请求用户**确认是否应用**识别结果；仅当用户确认应用后，MUST 将对应食物行**追加**到当前列表末尾（或按产品约定的合并策略）。每一行 MUST 在 UI 上可标注为「AI 识别」。持久化时，对应 `MealFoodItemRequest.recognitionSource` MUST 为 `ai`；用户通过搜索添加的行 MUST 为 `user_manual`（或与后端 `meal_food_item` 注释一致的枚举值）。Stub 阶段 MAY 保持「成功即自动追加」以跑通流程。

#### Scenario: 手动与 AI 行共存

- **WHEN** 用户先手动搜索添加「米饭」，再完成 AI 追加「鸡胸肉」
- **THEN** 列表中 MUST 存在两行，且各行 `recognitionSource` 分别反映手动与 AI

### Requirement: 上传图片后使用 stub 跑通识别流程

在真实 AI **异步**路径下，当用户上传图片并成功得到 `file_id` 后，应用 **SHALL NOT** 自动进入识别处理；应用 MUST 在后续由用户或页面逻辑**显式调用**后端「创建识别任务」接口后再进入 `aiRecognizing`（轮询）。在 Stub 未移除前，Stub 路径 MAY 仍在上传成功后短延迟模拟完成以跑通本地流程，但 SHALL NOT 与「上传接口内触发真实识别」混淆。

#### Scenario: 真实路径上传后不自动创建识别任务

- **WHEN** 用户仅完成上传并拿到 `file_id`，且未调用创建识别任务接口
- **THEN** 应用 MUST NOT 自动开始真实识别轮询

#### Scenario: Stub 完成后可保存

- **WHEN** stub 识别完成且列表至少有一行
- **THEN** 保存按钮 MUST 可点击，且 `recordMethod` 符合 `photo_ai` 约定

### Requirement: 识别过程与确认列表概念对齐

客户端 SHALL 区分 **识别过程输出**（任务 poll 返回的 `recognition`、候选食材等）与 **用户确认后的列表**（将写入保存请求的 `meal_food_item`）。在用户未提交保存前，识别结果的真源 MAY 仅为客户端草稿；最终保存时 SHALL 将用户确认后的 `meal_food_item` 与扩展后的 `meal_record` 一并提交。服务端 `meal_recognition_*` 是否在保存时写入由 `meal-record-save` 等既有契约决定，本变更不强制在 poll 阶段落库。

#### Scenario: 用户编辑后再保存

- **WHEN** 用户在识别完成后删除或修改某行食物再保存
- **THEN** 保存请求中的 `meal_food_item` SHALL 反映编辑后的终态；识别过程表若启用 SHALL 保留原始识别留痕（以服务端策略为准）
