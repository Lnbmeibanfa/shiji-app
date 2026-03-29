# 客户端架构规范（client-architecture）

本规范定义移动端（Flutter）的架构规则。

---

## 需求：客户端不得直接调用 AI 服务

客户端必须通过后端访问 AI 能力。

### 场景：用户触发 AI 分析

- 当客户端需要 AI 分析
- 必须调用后端 API
- 后端再调用 AI 模型服务

---

## 需求：客户端按功能模块组织代码

Flutter 项目必须采用 feature-first 结构。

### 场景：新增功能模块

- 当新增功能模块
- 必须在 `features` 目录中创建独立模块
- 页面、状态、模型必须放在同一模块内

---

## 需求：客户端不存储生产密钥

移动端不得保存生产环境 API key。

### 场景：AI接口调用

- API key 只能存在后端
- 客户端只调用后端服务

---

## 需求：界面实现须遵守 Design Token 规范

Flutter 界面实现 MUST 遵守 `openspec/specs/design-tokens/spec.md` 中的 Design Token v1：颜色、圆角、间距、阴影、排版层级与已定组件视觉规则 MUST 通过规范定义的 Token 或等价主题 API（如 `AppColors`、`AppSpacing`、`AppRadius`、`AppTypography`）引用，MUST NOT 在业务组件中随意硬编码十六进制色值或未文档化的尺寸。

### 场景：新增页面与组件

- 当开发者在 `features/` 下新增页面或通用组件
- 必须使用 Token 规范中的颜色与间距圆角
- 不得仅在 Widget 内新增未收敛的 `Color(0xFF...)`

### 场景：AI 生成代码

- 当使用 AI 生成 Flutter UI 代码
- 生成结果必须符合 `design-tokens` 规范中的约束（仅用 Token 常量、禁止在组件内随意写死色值与圆角间距）

---

这一条会避免一个大坑：

Flutter 直接调用 Gemini API。

---

## 需求：网络访问不得绕过 Repository 与 ApiClient

`features/` 下的页面与 Widget MUST NOT 直接依赖 `package:dio` 或原始 HTTP 客户端发起后端调用。所有后端 API 调用 MUST 通过 `core/network` 暴露的统一客户端及 `features/<feature>/repositories` 中的 Repository 完成。

### 场景：新增业务页面

- **WHEN** 开发者在某 feature 中新增页面需要拉取数据
- **THEN** 页面调用 Repository 或上层状态对象，而非直接 `dio.get/post`

---

## 需求：Token 不得在业务代码中散落

业务 feature MUST NOT 直接读写 token 字符串常量或调用 `flutter_secure_storage` 存取 token。会话读写 MUST 经 `AuthStorage`（或项目统一命名之会话封装）与认证控制器协调。

### 场景：保存登录结果

- **WHEN** 登录 API 返回 token
- **THEN** token 写入由 `AuthStorage`（或等价封装）执行，页面代码不直接持久化密钥字符串
