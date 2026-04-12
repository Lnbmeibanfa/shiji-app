# 记录饮食页 UI（record-meal-ui）

本规范定义 `apps/mobile` 中「记录饮食」全屏页的布局、组件边界、路由命名及与保存相关的客户端规则（不含 AI、不含最近一餐真实数据）。

---

## ADDED Requirements

### Requirement: 全屏路由 MUST 使用「记录饮食」语义且不得使用 /camera

应用 MUST 提供可寻址的全屏路由用于「记录饮食」页面，路径常量 MUST 使用独立命名（例如 `RoutePaths.recordMeal`，实现阶段与代码一致），且 MUST NOT 使用 `'/camera'` 作为该页面的对外路径。所有从首页、Shell 等入口进入记录流程的导航 MUST 指向该路径。

#### Scenario: 首页入口进入记录饮食

- **WHEN** 用户从首页入口触发「记录饮食」导航
- **THEN** 应用打开该全屏路由对应页面，且 URL/路径中不包含 `camera` 作为该功能标识

### Requirement: 页面 MUST 按设计分区展示并可滚动

记录饮食页 MUST 使用垂直滚动容器承载自上而下分区，且 MUST 至少包含：照片区（支持无图状态）、餐别单选、最近一餐区块（占位）、手动添加食物入口、可选情绪区、可选备注区、底部主操作「保存记录」。各区块 MUST 使用项目主题 token 保持间距与圆角一致性。

#### Scenario: 无照片时仍可浏览完整表单

- **WHEN** 用户未选择或未成功上传照片
- **THEN** 用户仍可见餐别、占位区块、手动添加、情绪、备注与保存区域（保存是否可点见「保存按钮可用性」要求）

### Requirement: 最近一餐区块 MUST 仅为占位

「最近一餐」相关区块 MUST 展示与当前所选餐别相关的标题文案（例如「最近的午餐」），**MUST NOT** 依赖后端接口或本地持久化数据填充列表；允许展示空态、骨架或静态占位卡片。

#### Scenario: 无网络时区块仍展示

- **WHEN** 设备无网络
- **THEN** 该区块仍渲染占位 UI，**MUST NOT** 因未实现接口而崩溃

### Requirement: 无照片时保存 payload 的 record_method MUST 为 manual

当用户未持有有效上传结果（无 `fileId` 或未上传成功）时，客户端在组装保存请求（或等价模型）时，字段 `record_method` **MUST** 为 `manual`。当存在有效上传得到的 `fileId` 时，`record_method` **MUST** 为 `photo`。

#### Scenario: 无图保存语义

- **WHEN** 用户未上传照片且后续点击保存（在满足食物条目前提下）
- **THEN** 请求体中 `record_method` 为 `manual`

#### Scenario: 有图保存语义

- **WHEN** 用户已成功上传并得到 `fileId` 且点击保存
- **THEN** 请求体中 `record_method` 为 `photo`

### Requirement: 保存按钮 MUST 在无任何食物项时禁用

主按钮「保存记录」**MUST** 在用户当前未添加任何食物项（`foodItems` 为空）时处于禁用态（不可点击且视觉上置灰）。**MUST** 在至少存在一条食物项时可点击（若同时存在其他校验失败，可后续扩展）。

#### Scenario: 无食物时不可保存

- **WHEN** 食物列表为空
- **THEN** 主按钮不可点且呈禁用样式

#### Scenario: 有食物时可点按

- **WHEN** 食物列表至少有一条
- **THEN** 主按钮可点击（不因「最近一餐」占位而阻塞）

### Requirement: 业务逻辑 MUST 按组件拆分再组装

页面实现 MUST 将照片区、餐别选择、最近占位、手动添加入口、情绪区、备注区、底栏保存拆为独立 Widget 文件（或等价模块化单元），再由页面级 Widget 组合；**MUST NOT** 在单文件内以超长 `build` 方法堆砌全部 UI 而无拆分。

#### Scenario: 代码结构可定位

- **WHEN** 维护者查找「餐别选择」样式
- **THEN** 可在独立组件源文件中找到对应 Widget，而非仅在页面主文件中检索
