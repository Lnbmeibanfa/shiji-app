# Flutter 会话、路由与全局反馈（flutter-client-bootstrap）— 增量

本文件为相对 `openspec/specs/flutter-client-bootstrap/spec.md` 的增量，归档时合并。

---

## MODIFIED Requirements

### Requirement: 全局错误提示

对可预期的 API 失败（含业务 `code != 0` 与网络错误），应用 MUST 通过统一入口（**`client-feedback` 规范所定义的 Toast 封装或其后继统一 API**）向用户展示简短说明，且文案 MUST 优先使用服务端返回的 `message`（若存在）。旧有仅包装默认 `SnackBar` 的入口 MAY 保留为委托实现或迁移别名，直至完全移除。

#### Scenario: 业务错误展示

- **WHEN** API 返回 `code != 0` 且 `message` 非空
- **THEN** 用户看到包含该信息的提示（符合 Toast 语义样式），而非无说明的白屏或仅控制台日志
