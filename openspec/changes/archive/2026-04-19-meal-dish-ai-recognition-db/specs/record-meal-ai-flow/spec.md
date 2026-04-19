# 记录饮食 AI 流程与添加食物 UI（record-meal-ai-flow）— 变更增量

本文件为 `openspec/specs/record-meal-ai-flow/spec.md` 的增量规范：对齐 **先标准菜品、后基础食物 fallback** 的识别架构，并为「识别过程」与「确认列表」分层预留产品与实现一致性。

---

## ADDED Requirements

### Requirement: 识别策略优先命中标准菜品再拆解基础食物

在接入真实图片识别后，应用层 **SHALL** 以「优先命中 `dish` / 标准菜品语义，未命中再拆解为多个 `food_item`」作为目标架构；Stub 阶段 MAY 继续仅向列表追加 `food_item` 行以跑通流程，但 SHALL NOT 阻碍后续切换为「先菜品后拆解」的数据写入路径。

#### Scenario: 真实识别命中菜品后的表示

- **WHEN** 后端/模型返回命中标准菜品且用户未拒绝该结果
- **THEN** 客户端 SHALL 能在一餐上下文中展示该命中（含名称与置信度等约定字段），并 SHALL 能继续进入用户确认与保存；具体 UI 形态 SHALL 与产品设计一致

### Requirement: 识别过程与确认列表概念对齐

客户端 SHALL 区分 **识别过程输出**（候选菜品、候选食物行、原始置信度）与 **用户确认后的列表**（将写入保存请求的 `meal_food_item`）；在持久化分层上，过程数据 SHALL 对应服务端 `meal_recognition_*`，最终保存 SHALL 对应 `meal_food_item` 与扩展后的 `meal_record`。

本要求 SHALL NOT 撤销既有「AI 成功时向列表追加行」的交互底线，直至产品另行规定合并/确认交互；演进时 SHALL 以增量方式兼容现有 `idle` / `aiRecognizing` 状态机。

#### Scenario: 用户编辑后再保存

- **WHEN** 用户在识别完成后删除或修改某行食物再保存
- **THEN** 保存请求中的 `meal_food_item` SHALL 反映编辑后的终态；识别过程表若启用 SHALL 保留原始识别留痕（以服务端策略为准）
