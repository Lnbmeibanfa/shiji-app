## MODIFIED Requirements

### Requirement: 服务端采用分层架构
后端必须采用标准分层结构，并在认证能力实现中严格遵循模块边界。

#### Scenario: 新增接口

- Controller 负责接收请求
- Service 负责业务逻辑
- Repository 负责数据库访问
- 外部服务调用必须封装在独立模块
- 认证相关能力（验证码、登录、会话、退出）MUST 通过独立业务模块组织，并遵守 Controller/Service/Repository 分层，不得将业务编排写入 Controller
