## Why

记录饮食流程需要从相册或相机选择餐食照片并持久化到 OSS；后端已提供 `POST /api/files/upload` 与 `file-upload` 规范，但移动端尚未接通 multipart 与 **Camera（`/camera`）** 页面。本变更先打通端到端上传与预览，为后续 AI 识别与保存一餐留好 `fileId`。

## What Changes

- 在 Flutter 客户端增加**饮食照片上传**能力：经统一 `ApiClient` 发起 `multipart/form-data` 上传，字段名 `file`，解析 `ApiResponse` 中的 `fileId` 与 `url`。
- **新增独立前端路由**（全屏 **Camera** 页，path 如 `/camera`），用户通过 `push` 进入；该页实现选图/拍照、**上传中态**、OSS 成功后预览、清除重选（不实现 AI 与保存一餐）。**不**将上述交互仅嵌在 Tab 根视图而无独立路由栈。
- 扩展网络层：在遵守「单一 HTTP 出口」前提下，为 `ApiClient`（或等价封装）增加 multipart 上传方法，供 Repository 复用。

## Capabilities

### New Capabilities

- `meal-photo-upload-ui`：定义 **Camera 页**上的选图、OSS 上传、预览与错误反馈，及与后端 `file-upload` 契约的对齐方式。

### Modified Capabilities

- `flutter-http-contract`：在不变更既有 JSON POST 约定的前提下，**新增**「经 `ApiClient` 的 multipart 上传与 `ApiResponse` 解析」要求，避免业务层私自 new `Dio` 发文件。

## Impact

- **代码**：`apps/mobile/lib/core/network/`（`ApiClient` 等）、`core/routing/`（如 `RoutePaths.camera` → `/camera`，并在 `app_router` 注册全屏 `GoRoute`）、`features/` 下新增 Camera 页与 Repository、`providers.dart`；首页「拍一顿」等入口 `push` 至该路由。
- **API**：复用现有 `POST /api/files/upload`，无后端变更。
- **依赖**：沿用现有 `dio`、`image_picker`（若项目未声明则需 `pubspec.yaml` 增加）。
