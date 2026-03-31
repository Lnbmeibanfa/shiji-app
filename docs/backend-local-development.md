# 食迹后端本地开发说明

面向后续接入本仓库的开发者：按本文档可在本机快速拉起 **MySQL、Redis** 并启动 **Spring Boot API**。

---

## 1. 项目简介

**食迹（Shiji）** 后端为 `services/api` 下的 Spring Boot 应用，负责业务 API、数据持久化及 AI 能力编排（与 Gemini 等对接在后续模块中实现）。  
本地开发默认通过 Docker Compose 提供 MySQL 与 Redis，应用通过 `application.yml` 连接本机映射端口。

---

## 2. 技术栈

| 类别 | 说明 |
|------|------|
| 运行时 | JDK 17（以 `services/api/pom.xml` 为准） |
| 框架 | Spring Boot |
| 构建 | Maven（可用项目内 `./mvnw`） |
| 数据库 | MySQL 8（Docker 镜像 `mysql:8.0`） |
| 缓存 | Redis 7（Docker 镜像 `redis:7`） |
| 编排 | Docker Compose（仓库根目录 `docker-compose.yml`） |

---

## 3. 本地环境依赖

在启动后端前，请确认已安装：

- **JDK 17**（或与 `pom.xml` 中 `java.version` 一致）
- **Docker Desktop**（或兼容的 Docker 引擎）与 **Docker Compose v2**
- **Git**（克隆仓库）

可选：

- **IDE**：IntelliJ IDEA / VS Code（含 Java 扩展）

> 若不使用 Docker，可自行安装 MySQL 与 Redis，并修改 `services/api/src/main/resources/application.yml` 中的地址与端口；本文以下按 **仓库默认 Docker 配置** 说明。

---

## 4. Docker 启动方式

在**仓库根目录**执行：

```bash
docker compose up -d
```

验证容器已运行：

```bash
docker compose ps
```

应看到 `shiji-mysql`、`shiji-redis` 为运行状态。

停止并移除容器（数据卷默认保留，数据不丢）：

```bash
docker compose down
```

如需连数据一并清理（慎用）：

```bash
docker compose down -v
```

---

## 5. MySQL 与 Redis 连接信息

以下与仓库内 `docker-compose.yml`、`application.yml` 一致，**从宿主机访问**时使用。

### MySQL

| 项 | 值 |
|----|-----|
| 宿主机地址 | `127.0.0.1` |
| 宿主机端口 | **3307**（映射容器内 `3306`） |
| 数据库名 | `shiji` |
| 用户名 | `shiji` |
| 密码 | `shiji123456` |
| JDBC URL（Spring 已配置） | `jdbc:mysql://127.0.0.1:3307/shiji?...` |

> 容器内 root 密码为 `root123456`，一般本地开发使用应用用户 `shiji` 即可。

### Redis

| 项 | 值 |
|----|-----|
| 地址 | `127.0.0.1` |
| 端口 | **6379** |

Spring Boot 在 `application.yml` 的 `spring.data.redis` 下已配置上述主机与端口。

### 阿里云 OSS（后端代理上传）

图片上传接口使用阿里云 OSS **PutObject**。默认 `shiji.oss.enabled=false`，未配置时接口返回业务错误「对象存储未配置」。

本地联调真实上传时：

1. 在阿里云创建 Bucket（与 `endpoint` 地域一致），建议使用 RAM 子账号，权限收敛到该 Bucket 的 `oss:PutObject` 等最小集。
2. 在 shell 或 IDE 环境变量中设置（**勿**把真实密钥写入仓库）：

| 变量 | 说明 |
|------|------|
| `OSS_ENDPOINT` | 如 `https://oss-cn-hangzhou.aliyuncs.com` |
| `OSS_REGION` | 如 `cn-hangzhou` |
| `OSS_BUCKET` | Bucket 名称 |
| `OSS_ACCESS_KEY_ID` | RAM 子账号 AccessKey ID |
| `OSS_ACCESS_KEY_SECRET` | RAM 子账号 AccessKey Secret |

3. 在 `application.yml` 或本地 `application-local.yml` 中设置 `shiji.oss.enabled=true`（或通过 Spring Profile 覆盖）。

若 Bucket 为**私有读**，返回的 `url` 可能无法在浏览器直接打开，需后续改为签名 URL 或绑定 CDN；与 `openspec/changes/api-oss-proxy-upload-v1` 设计一致。

---

## 6. Spring Boot 启动方式

1. 确保已执行 `docker compose up -d`，且 MySQL / Redis 健康。
2. 连接配置位于：  
   `services/api/src/main/resources/application.yml`  
   无需再改即可对接本仓库默认 Docker 端口。
3. 在终端进入 API 目录并启动：

```bash
cd services/api
./mvnw spring-boot:run
```

**Windows（CMD/PowerShell）** 若无可执行权限，可使用：

```bash
cd services\api
mvnw.cmd spring-boot:run
```

入口类：`com.shiji.api.ShijiApiApplication`。

### 常用 Maven 命令

```bash
cd services/api
./mvnw clean test      # 仅跑测试
./mvnw clean package   # 打包（跳过测试可加 -DskipTests，不推荐作为默认习惯）
```

---

## 7. 常用排查命令

### Docker / 容器

```bash
docker compose logs -f mysql    # 查看 MySQL 日志
docker compose logs -f redis    # 查看 Redis 日志
docker compose ps               # 容器状态
docker inspect shiji-mysql      # 检查 MySQL 容器网络与端口映射
```

### MySQL 连通性（本机已装 mysql 客户端时）

```bash
mysql -h 127.0.0.1 -P 3307 -u shiji -pshiji123456 -D shiji -e "SELECT 1;"
```

### Redis 连通性（本机已装 redis-cli 时）

```bash
redis-cli -h 127.0.0.1 -p 6379 ping
# 期望返回 PONG
```

### Spring Boot 启动失败

- **连接被拒绝**：确认 `docker compose ps` 中 MySQL/Redis 已 Up，且端口未被本机其他程序占用（3307、6379）。
- **Access denied**：核对用户名/密码与本文第 5 节、`docker-compose.yml` 是否一致。
- **时区/编码**：当前 JDBC URL 已带 `serverTimezone=Asia/Shanghai`、`characterEncoding=utf8`；若改库地址，请保留或等价配置。

---

## 8. 与仓库其他文档的关系

- 全局规范与流程见根目录 `README.md`、`docs/development-guide.md`。
- 接口契约与行为变更以 `openspec/specs/`、`openspec/changes/` 为准；本文仅描述**本地运行基础设施与后端进程**。

配置变更（端口、库名、账号）时，请**同步修改**：

- `docker-compose.yml`
- `services/api/src/main/resources/application.yml`
- 并在本文件更新对应表格，避免后来者踩坑。
