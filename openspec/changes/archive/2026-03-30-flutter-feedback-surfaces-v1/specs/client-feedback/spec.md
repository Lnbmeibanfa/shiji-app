# 客户端反馈表面（client-feedback）— 增量

本文件定义 Toast、Banner、确认 Dialog 的行为与交互契约；归档后合并入 `openspec/specs/client-feedback/spec.md`。

---

## ADDED Requirements

### Requirement: 统一 Toast 表面

系统 MUST 提供全局可调用的 Toast 能力，用于短时非阻塞反馈。Toast MUST 支持主标题与副标题两行文案；MUST 区分语义：**成功**、**失败**、**提示（浅底/毛玻璃）**、**提示（暖色实底）**。背景色 MUST 仅使用 Design Token 中已定稿的反馈色（见 `design-tokens` 增量）。Toast MUST 在展示 **3 至 5 秒** 后自动消失；新 Toast 展示时 SHOULD 取代当前可见 Toast，避免堆叠遮挡。

#### Scenario: 成功反馈

- **WHEN** 业务调用成功语义 Toast，并传入标题与副标题
- **THEN** 用户看到胶囊形、轻阴影的成功样式（绿色调），并在数秒内自动消失

#### Scenario: 失败反馈

- **WHEN** 业务调用失败语义 Toast
- **THEN** 用户看到暖橙调失败样式（非刺红），并在数秒内自动消失

#### Scenario: 提示反馈（两档）

- **WHEN** 业务调用浅底毛玻璃提示或暖色实底提示
- **THEN** 用户看到与设计稿一致的对应视觉档位，并在数秒内自动消失

---

### Requirement: 公告 Banner 表面

系统 MUST 提供 Banner 能力，用于顶区或内容区上方的轻量通告。Banner MUST 使用规范定稿的背景色 Token；MUST 展示主文案；MUST 提供可选关闭控件；MUST 支持可选 **「查看详情」** 操作，且 **点击行为 MUST 由父级/调用方通过回调注入**。Banner MUST 支持用户手动关闭；MAY 支持在指定时间后自动关闭（由调用方传入时长或开关）。

#### Scenario: 查看详情

- **WHEN** 调用方传入「查看详情」回调且用户点击该链接
- **THEN** 执行调用方提供的逻辑（如打开 WebView 或路由跳转）

#### Scenario: 关闭

- **WHEN** 用户点击关闭或自动关闭时间到达
- **THEN** Banner 从界面移除且不再拦截交互

---

### Requirement: 确认 Dialog 表面

系统 MUST 提供确认类弹窗，包含标题、正文、次要操作（取消）与主要操作（确认）。弹窗 MUST 使用大圆角、白底与轻阴影；主操作按钮 MUST 使用规范定稿的成功/主绿色 Token，MUST NOT 使用刺红作为主强调。弹窗 MUST 提供右上角关闭；取消与确认的行为 MUST 由调用方回调定义。

#### Scenario: 用户取消

- **WHEN** 用户点击取消或关闭且未确认
- **THEN** 弹窗关闭且不执行确认侧业务（除非调用方另行约定）

---

### Requirement: 反馈组件不得散落硬编码色值

实现 Toast、Banner、Dialog 的 Widget 与调用封装 MUST 仅引用 `AppColors` / `AppSpacing` / `AppRadius` / `AppShadows`（及排版 Token）中的常量，MUST NOT 在 `build` 中直接书写 `Color(0xFF...)`（`AppColors` 定义文件内部除外）。

#### Scenario: Code review

- **WHEN** 审查新增反馈相关 Dart 文件
- **THEN** 不应出现业务文件内联十六进制颜色字面量
