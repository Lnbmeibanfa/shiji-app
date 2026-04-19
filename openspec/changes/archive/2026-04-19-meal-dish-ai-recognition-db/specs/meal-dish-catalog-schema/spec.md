# 标准菜品目录数据模型（meal-dish-catalog-schema）

本规范定义「标准菜品层」及相关关联表的数据语义与约束，与 `food_item` 标准基础食物库协同。

---

## ADDED Requirements

### Requirement: dish 表示用户感知层标准菜品或商品

系统 SHALL 提供 `dish` 实体，用于存储平台级标准菜品/商品（如「番茄炒蛋」「奶茶」「套餐」），供 AI 图片识别 **优先命中**；其 SHALL NOT 等同于某一用户的某一餐记录。

`dish` SHALL 至少区分 **来源** 与 **品类**：来源由 `dish_source_type` 表示（如 `system_standard`、`takeout_candidate`、`merchant_imported`、`ai_generated`、`manual`）；品类由 `dish_kind` 表示（如 `dish`、`drink`、`dessert`、`package_meal`）。MVP 阶段外卖候选菜品 SHALL 与标准菜品 **共表** 管理，SHALL NOT 强制单独 `external_dish` 表。

#### Scenario: 标准库菜品可入库

- **WHEN** 运营或系统录入一条标准菜品且 `dish_code` 在系统内唯一
- **THEN** 该记录 SHALL 可被持久化，并 SHALL 可通过主键被 `meal_record` 或识别流程引用

#### Scenario: 外卖候选可与标准菜品共表

- **WHEN** 一条外卖商品以 `dish_source_type=takeout_candidate` 入库
- **THEN** 其 SHALL 仍存储于 `dish` 表，且 SHALL 可通过来源字段与标准库区分

### Requirement: dish_alias 支持别名与命中匹配

系统 SHALL 提供 `dish_alias`（或等价命名）实体，用于存储菜品的 **别名、同义词、常见误写**，以支持 OCR/语音识别/AI 输出名称与标准名对齐。

#### Scenario: 别名指向标准菜品

- **WHEN** 存在别名「西红柿炒鸡蛋」映射至标准菜品「番茄炒蛋」
- **THEN** 查询或匹配逻辑 SHALL 能经由别名解析到对应 `dish` 主记录

### Requirement: dish_food_item_rel 表达菜品与基础食物的可选组成

系统 SHALL 提供 `dish_food_item_rel`，用于建立 `dish` 与 `food_item` 之间的 **可选** 组成关系；关系 MAY 包含角色、默认重量、比例、是否可选、排序等字段。

系统 SHALL NOT 要求每个 `dish` 必须存在至少一条 `dish_food_item_rel`（例如奶茶、果冻、难拆解套餐可仅有 `dish` 而无拆解）。

#### Scenario: 可拆解菜品有关联行

- **WHEN** 「番茄炒蛋」在业务上拆解为番茄、鸡蛋、油
- **THEN** 系统 SHALL 能通过多条 `dish_food_item_rel` 表达该组成

#### Scenario: 不可拆解商品可无关联行

- **WHEN** 某商品被标记为不拆解或业务未维护组成
- **THEN** 该 `dish` SHALL 允许不存在任何 `dish_food_item_rel` 行

### Requirement: 本阶段不强制 dish_nutrition 物化表

系统 SHALL 以 `food_nutrition` 为营养真源；菜品级营养 SHALL 默认通过 `dish_food_item_rel` 关联的 `food_item` 与 `food_nutrition` **聚合计算**。若未来存在性能或快照需求，再引入 `dish_nutrition` 物化表（本规范范围外）。

#### Scenario: 聚合营养可推导

- **WHEN** 某 `dish` 已配置组成且各 `food_item` 存在营养数据
- **THEN** 系统 SHALL 能通过聚合得到该菜品的估算营养（实现可为查询时计算或缓存策略）
