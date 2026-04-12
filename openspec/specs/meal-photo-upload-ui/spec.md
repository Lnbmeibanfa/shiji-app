# 饮食照片上传 UI（meal-photo-upload-ui）

本规范定义 **Camera（相机）页**上的选图、上传 OSS 与预览行为；与后端 `file-upload` 能力配合，不包含 AI、食物明细与保存一餐。页面产品名称为 **Camera**，路由 path **`/camera`**；**不**使用「上传页」作为该屏名称。

---

## 规范要求

### Requirement: Camera 页 MUST 位于独立可寻址路由

选图、拍照、上传 OSS、预览与清除 MUST 实现在 **Camera 页**（全屏 `GoRoute`，path **`/camera`**，常量如 `RoutePaths.camera`）。用户 MUST 通过导航栈进入（如 `push`），并 MUST 能通过返回控件回到进入前的界面（如 `pop`）。客户端 MUST NOT 将 Camera 交互仅作为「记录」Tab 根视图的内嵌内容而无独立路由目标。

#### Scenario: 从首页进入 Camera 页

- **WHEN** 用户在已登录状态下从首页入口（如「拍一顿」）触发导航
- **THEN** 应用导航至 `/camera`，且该路由与主壳 Tab 路由可区分

#### Scenario: 返回退出 Camera 页

- **WHEN** 用户在 Camera 页触发系统或顶栏返回
- **THEN** 当前路由从栈上移除，回到进入前的页面

### Requirement: 已登录用户可通过相册或相机选择餐食图片

客户端 MUST 在 **Camera 页**提供至少两个入口：**从相册选择**与**使用相机拍摄**（在目标平台支持的前提下）。选择结果 MUST 以可上传的二进制形式（如 `XFile`）暂存于内存或页面状态中，直至提交 OSS 或清除。

#### Scenario: 用户从相册选择一张图

- **WHEN** 用户点击相册入口且系统返回一张图片
- **THEN** 客户端持有该图片引用并可在本地或上传成功后用于预览

#### Scenario: 用户使用相机拍照

- **WHEN** 用户点击相机入口且系统返回拍摄结果
- **THEN** 客户端持有该图片引用并可在上传成功后用于预览

### Requirement: 上传请求 MUST 经 ApiClient 使用 multipart 调用后端

客户端 MUST 使用 `core/network` 中 `ApiClient`（或其在规范上的唯一封装）向 `POST /api/files/upload` 发送 `multipart/form-data` 请求，且 MUST 使用表单字段名 `file`。业务页面与 Widget MUST NOT 为上传单独创建未受约束的 `Dio` 实例。请求 MUST 携带与既有 JSON 接口相同的鉴权与基地址配置。

#### Scenario: 上传成功解析 fileId 与 url

- **WHEN** 响应体为 `ApiResponse` 且 `code == 0`，且 `data` 包含 `fileId` 与 `url`
- **THEN** 业务层获得解析后的 `fileId` 与 `url` 供后续展示或持久状态使用

#### Scenario: 业务失败

- **WHEN** 响应体中 `code != 0`
- **THEN** 客户端 MUST 按统一业务错误处理，且 MUST NOT 将本次上传视为成功

### Requirement: 上传过程中 MUST 向用户反馈进行态

客户端 MUST 在上传进行中展示可感知的加载状态（如按钮禁用、全屏或局部 loading），且 MUST 避免重复并发提交同一文件。

#### Scenario: 上传进行中

- **WHEN** 上传请求已发出且尚未结束
- **THEN** 用户可见加载反馈，且主要操作入口处于不可重复触发状态或等价防重

### Requirement: 上传成功后 MUST 展示可访问预览

客户端 MUST 使用服务端返回的 `url`（或项目约定的等价展示方式）展示餐食图片预览。预览 MUST 在成功路径下可辨识为所选餐食照片。

#### Scenario: 展示网络预览

- **WHEN** 上传成功且 `url` 非空
- **THEN** 界面展示该图片的网络预览

### Requirement: 用户 MUST 能清除已选/已传图片以重新选择

客户端 MUST 提供清除或关闭入口（如预览区关闭按钮），清除后 MUST 丢弃当前 `fileId`、`url` 与本地文件引用，界面回到可重新选择状态。

#### Scenario: 清除后重新上传

- **WHEN** 用户触发清除且再次选择图片并上传成功
- **THEN** 预览与内存状态与新一次上传结果一致

### Requirement: 失败时 MUST 提供用户可读反馈

当网络错误、超时或业务 `code != 0` 时，客户端 MUST 通过项目统一反馈机制（如 SnackBar）展示可读提示，且 MUST NOT 静默失败。

#### Scenario: 上传失败提示

- **WHEN** 上传以网络异常或业务错误结束
- **THEN** 用户看到明确失败提示，且界面允许重试或重新选择
