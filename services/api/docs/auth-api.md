# 认证 HTTP 接口（`/api/auth`）

统一响应体：`{ "code": number, "message": string, "data": object | null }`。业务成功时 `code` 为 `0`，`message` 一般为 `"success"`。

## 发送验证码

`POST /api/auth/sms/send`

**请求体**

| 字段 | 类型 | 约束 |
|------|------|------|
| `phone` | string | 必填，`^1\d{10}$` |
| `deviceId` | string | 可选 |

**成功示例**

```json
{ "code": 0, "message": "success", "data": null }
```

开发环境使用 `SmsGatewayStub`：验证码不会真实下发，请在服务日志中搜索 `SMS stub sent` 获取明文验证码。

## 短信验证码登录

`POST /api/auth/login/sms`

**请求体**

| 字段 | 类型 | 约束 |
|------|------|------|
| `phone` | string | 必填 |
| `code` | string | 必填，4–8 位数字 |
| `deviceId` | string | 可选 |
| `agreements` | array | 必填非空；每项含 `agreementType`（`USER_AGREEMENT` \| `PRIVACY_POLICY`）、`agreementVersion`、`accepted`（须为 `true`） |

**成功示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 1,
    "token": "<明文 token，请客户端保存>",
    "expireAt": "2026-04-26T12:00:00",
    "newUser": true
  }
}
```

## 会话恢复

`POST /api/auth/session/restore`

**请求体**

| 字段 | 类型 | 约束 |
|------|------|------|
| `token` | string | 必填 |

**成功示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 1,
    "phone": "13800138000",
    "expireAt": "2026-04-26T12:00:00"
  }
}
```

## 全部退出（需登录）

`POST /api/auth/logout/all`

**请求头**

- `Authorization: Bearer <token>`

**成功示例**

```json
{ "code": 0, "message": "success", "data": null }
```

未带有效 Bearer 时 HTTP 状态为 `401`，body 仍为统一结构，`code` 为 `10009`（见下表）。

## 认证相关错误码（`AuthErrorCode`）

| code | 说明 |
|------|------|
| 10001 | 验证码发送过于频繁 |
| 10002 | 验证码发送次数超过限制 |
| 10003 | 验证码不存在或已失效 |
| 10004 | 验证码已过期 |
| 10005 | 验证码错误 |
| 10006 | 验证码错误次数超限 |
| 10007 | 未同意协议，禁止登录 |
| 10008 | 会话无效 |
| 10009 | 未登录或登录已失效 |

业务失败时 HTTP 状态多为 `200`，通过 `code != 0` 区分；`401` 仅用于未认证访问需登录接口。

## 本地联调（curl 示例）

将 `BASE` 换成本地网关地址（如 `http://localhost:8080`），登录前从日志读取验证码替换 `YOUR_CODE`。

```bash
# 1. 发送验证码
curl -s -X POST "$BASE/api/auth/sms/send" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'

# 2. 登录（agreements 须与产品约定版本一致）
curl -s -X POST "$BASE/api/auth/login/sms" \
  -H "Content-Type: application/json" \
  -d '{
    "phone":"13800138000",
    "code":"YOUR_CODE",
    "agreements":[
      {"agreementType":"USER_AGREEMENT","agreementVersion":"v1","accepted":true},
      {"agreementType":"PRIVACY_POLICY","agreementVersion":"v1","accepted":true}
    ]
  }'

# 3. 会话恢复（将上一步返回的 token 填入）
curl -s -X POST "$BASE/api/auth/session/restore" \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_TOKEN"}'

# 4. 全部退出
curl -s -X POST "$BASE/api/auth/logout/all" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

移动端联调：将上述 `BASE` 配置为同一局域网内机器地址，按相同顺序调用即可。

## 数据库补丁（`sms_code_log`）

发码频控与「最新一条待验证记录」查询依赖列 `sent_at`。若 MySQL 表为早期手工创建、缺少该列，会出现 `Unknown column 'sent_at' in 'where clause'`。在业务库执行一次：

`docs/schema-patches/mysql-sms-code-log-add-sent-at.sql`

若补丁后 **插入** 仍失败（例如存在遗留必填列 `biz_type`、或 `status`/`error_count` 等与 `SmsCodeLogEntity` 不一致），说明本地表与当前代码模型差异较大；**开发环境**可备份后执行按实体重建脚本（会清空该表数据）：

`docs/schema-patches/mysql-sms-code-log-recreate-dev.sql`

### `user` / `user_auth` / `user_session`（与遗留库列名对齐）

代码侧已通过 JPA 映射兼容常见遗留列名，无需改表即可联调：

- `user.phone`（Java）→ 列 **`user_no`**
- `user_auth.auth_identifier` → **`auth_key`**
- `user_session.token_hash` → **`session_token`**；`session_status` 为 **tinyint**（1 活跃 / 2 作废 等）；`client_ip` → **`login_ip`**；并需 **`login_type`**（登录成功时写入 `PHONE_SMS`）、**`login_at`**

若遗留库缺少 `user_session.user_agent`，执行一次：`docs/schema-patches/mysql-user-session-add-user-agent.sql`
