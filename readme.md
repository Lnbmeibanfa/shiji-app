# 食迹（Shiji）

## 项目介绍

食迹是一款 **AI 驱动的饮食记录与健康分析应用**。  
项目目标是让用户以尽可能低的成本记录每日饮食，并通过后端统一管理的 AI 能力，对饮食结构、营养摄入与长期健康趋势进行分析，为用户生成可执行的健康建议和报告。

本仓库采用 **OpenSpec + Cursor + 测试驱动开发（TDD）** 的方式进行规范化迭代，所有规范与文档均使用中文。

## 核心功能概览（规划中）

> 以下功能会在对应的 Spec 与 Change 完成后逐步落地，当前仓库只提供架构与目录骨架。

- **饮食记录（Food Record）**
  - 支持图文记录每日饮食
  - 记录时间、餐别、食物明细等结构化信息
- **AI 饮食分析（AI Analysis）**
  - 基于 Gemini 对单次饮食进行营养与健康分析
  - 聚合一定周期后输出趋势与问题提示
- **用户与档案管理（User & Profile）**
  - 用户基础信息与健康目标管理
  - 偏好与禁忌食物记录
- **健康报告（Health Report）**
  - 周/月/季度健康报告生成
  - 针对饮食习惯给出可执行建议

## 技术栈说明

- **移动端**：Flutter（单代码库多平台）
- **后端**：Spring Boot
- **数据库**：MySQL（持久化用户、饮食记录与报告等核心数据）
- **缓存**：Redis（会话、热点数据与部分计算结果缓存）
- **AI 能力**：Gemini API，由后端统一调用和管理
- **开发方式**：
  - OpenSpec 规范驱动
  - Cursor 辅助协作
  - 测试驱动开发（TDD）

## 仓库目录结构说明

项目采用多应用单仓库结构（mono-repo），顶层目录如下：

- `apps/`：前端应用
  - `mobile/`：Flutter 移动端应用
- `services/`：后端服务
  - `api/`：基于 Spring Boot 的主 API 服务
- `docs/`：项目文档与架构说明（全部中文）
- `openspec/`：OpenSpec 规范与变更（全部中文）
- `scripts/`：开发、构建与运维相关脚本
- `.cursor/`：Cursor 项目规则与 AI 配置

### 前端（Flutter）目录结构

- `apps/mobile/lib/app/`：应用级入口与配置（路由、主题等）
- `apps/mobile/lib/core/`：跨业务复用的核心能力
  - `network/`：网络与 API client
  - `storage/`：本地存储抽象
  - `utils/`：通用工具
- `apps/mobile/lib/features/`：按业务模块划分
  - `home/`：首页模块（`pages/`、`widgets/`、`state/`）
  - `food_record/`：饮食记录模块（`pages/`、`widgets/`、`state/`、`models/`、`services/`）
  - `ai_analysis/`：AI 分析模块（`pages/`、`widgets/`、`state/`、`models/`、`services/`）
  - `profile/`：个人信息与设置模块（`pages/`、`widgets/`、`state/`）

测试目录：

- `apps/mobile/test/features/`
  - `home/`
  - `food_record/`
  - `ai_analysis/`
  - `profile/`

### 后端（Spring Boot）目录结构

根包为 `com.shiji.api`，在此之下按「基础设施 + 业务模块」划分：

- `common/`：通用模型、统一响应结构、异常处理等
- `config/`：应用与框架配置（如 Spring、数据库、缓存）
- `infrastructure/`
  - `ai/`：AI 客户端与 Gemini 调用适配
  - `persistence/`：数据库访问基础设施
  - `cache/`：Redis 等缓存封装
- `modules/`：按业务模块划分
  - `user/`
    - `controller/`
    - `service/`
    - `repository/`
    - `model/entity|dto|vo/`
  - `food_record/`
  - `ai_analysis/`
  - `health_report/`

测试目录：

- `services/api/src/test/java/com/shiji/api/modules/`
  - `user/`
  - `food_record/`
  - `ai_analysis/`
  - `health_report/`

## 为什么前后端都采用按业务模块划分

- **对齐领域边界**：  
  以业务领域（用户、饮食记录、AI 分析、健康报告）作为模块边界，比按技术层（controller/service/repository）平铺更贴近真实需求。

- **前后端结构一致**：  
  Flutter 的 `features/*` 与 Spring Boot 的 `modules/*` 在命名与边界上保持一致，便于前后端协作与需求沟通。

- **提升可维护性与可测试性**：  
  每个模块内部自包含 Controller / Service / Repository / Model / Test，更利于模块级重构、替换和拆分。

- **为演进架构预留空间**：  
  当需要拆分出独立服务或 BFF 时，可以以现有模块为单位平滑迁移。

## 开发规范说明

### 文档全部使用中文

- `README.md`、`docs/`、`openspec/` 等文档必须使用中文编写。
- 如需引用英文术语（如 TDD、OpenSpec），应在中文上下文中简要解释其含义。

### Spec 驱动开发（OpenSpec）

- 所有功能与行为以 `openspec/specs/` 与 `openspec/changes/` 为唯一事实来源。
- **禁止** 直接在代码中凭主观经验实现新功能，必须先有规范：
  - 新领域规则 -> 新增或更新 `openspec/specs/*`
  - 具体功能或改动 -> 新增 `openspec/changes/*`
- 当实现与规范不一致时：
  - 优先修改实现以符合当前规范；
  - 如业务确需调整行为，则通过新的 Change 变更规范。

### 测试驱动开发（TDD）

- 新功能的实现顺序必须遵守：**Spec → Change → Test → Code → Refactor**。
- 在开始写实现代码前：
  - 前端需在 `apps/mobile/test/features/<feature>/` 下写出或补充测试
  - 后端需在 `services/api/src/test/java/com/shiji/api/modules/<module>/` 下写出或补充测试
- 单元测试与集成测试应覆盖：
  - 主成功路径
  - 关键边界条件
  - 主要异常分支

### Cursor 协作方式

- 仓库内通过 `.cursor/rules/` 以及 OpenSpec 规范约束 AI 的行为：
  - AI 生成代码必须遵守现有目录结构与模块边界
  - AI 生成实现时，同时生成对应测试骨架或完整测试
  - AI 不得直接在 Flutter 客户端写入生产环境 Gemini API Key，所有 AI 能力通过后端统一封装
- 建议在使用 Cursor 进行重构或新增功能时：
  - 先指引其阅读相关 `openspec/specs/*` 与 `openspec/changes/*`
  - 明确说明当前正在修改的业务模块（如 `food_record` 或 `ai_analysis`）

## 本地开发启动方式

### 前置条件

- Flutter SDK（版本以 `apps/mobile/pubspec.yaml` 为准）
- JDK 17（或以 `services/api/pom.xml` 为准）
- MySQL 与 Redis 实例（本地或容器均可）
- Maven 或使用项目自带的 `mvnw`

### 启动 Flutter 移动端

```bash
cd apps/mobile
flutter pub get
flutter run
```

当前 `main.dart` 仅提供占位页面，后续实际页面会从 `features/` 模块接入。

### 启动 Spring Boot 后端

确保本地 MySQL 与 Redis 可用，并在 `services/api/src/main/resources/application.properties` 中配置好连接信息：

```bash
cd services/api
./mvnw spring-boot:run
```

应用入口类为 `com.shiji.api.ShijiApiApplication`，实际业务接口会在 `modules/*` 下逐步实现。

### 运行测试

- Flutter 测试：

```bash
cd apps/mobile
flutter test
```

- 后端测试：

```bash
cd services/api
./mvnw test
```

建议在每次提交前保持前后端测试全部通过。

## 推荐开发流程：Spec → Change → Test → Code → Refactor

1. **Spec：定义规范**
   - 在 `openspec/specs/` 中新增或更新对应规范（例如客户端架构、AI 能力、后端服务架构等）。
2. **Change：描述变更**
   - 在 `openspec/changes/` 中新增变更文档，描述本次迭代的场景、输入输出与边界。
3. **Test：先写测试**
   - 前端在对应 `features/<feature>/` 下新增或修改测试；
   - 后端在对应 `modules/<module>/` 下新增或修改测试。
4. **Code：实现功能**
   - 按模块边界在正确的目录内实现代码，保证实现逻辑紧贴 Spec 与 Change。
5. **Refactor：重构与清理**
   - 在测试全部通过的前提下，对内部结构进行重构；
   - 如重构影响行为，应回到 Spec / Change 更新规范。

整个流程的目标是：**规范可追溯、实现可测试、行为可演进**。
