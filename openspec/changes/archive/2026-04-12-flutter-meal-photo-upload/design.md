## Context

后端已实现 `POST /api/files/upload`（`multipart/form-data`，字段 `file`），规范见 `openspec/specs/file-upload/spec.md`。Flutter 侧 `ApiClient` 当前仅封装 JSON `POST`，尚未提供 multipart；`apps/mobile` 无 `image_picker`。记录饮食完整页（AI、食物行、标签）尚未建设，需先落地 **Camera 页**上的选图、OSS 上传与预览，并保留 `fileId` 供后续步骤使用。

## Goals / Non-Goals

**Goals:**

- 通过统一 `ApiClient` 调用 `/api/files/upload`，解析 `ApiResponse` 得到 `fileId` 与可展示 `url`。
- **独立路由 `/camera`（相机页 / Camera）**：相册、相机、上传中态、成功预览、清除重选；用户通过 `context.push`（或等价）进入，通过 AppBar 返回或 `pop` 退出。
- 错误与加载态符合 `client-feedback` 约定（SnackBar / 加载指示等现有模式）。

**Non-Goals:**

- AI 识别、食物库检索、分量编辑、`meal_record` 保存、`meal_tag`。
- 后端或 OSS 配置变更；STS 直传。
- 缩略图异步生成（后端字段已有，客户端仅用主 `url`）。

## Decisions

1. **网络层：在 `ApiClient` 增加 `postMultipart`（或 `uploadFile`）**  
   - **理由**：满足 `flutter-http-contract`「单一 HTTP 出口」；`Dio` 使用 `FormData` + `MultipartFile`，响应体仍走 `ApiEnvelope.parse`，与 JSON 接口一致。  
   - **备选**：在 Repository 内临时 `Dio.post` — 被拒绝，违反规范。

2. **字段与路径**  
   - 路径：`/api/files/upload`（相对 `AppConfig.apiBaseUrl`）。  
   - 表单字段名：`file`（与后端 `FileUploadController` 一致）。  
   - 成功 `data`：`fileId`、`url`、`objectKey`、`bucket`、`contentType`、`size` — 客户端至少解析 `fileId` 与 `url`。

3. **Feature 分层**  
   - `features/meal_record/`（或 `record_meal/`）下：`meal_photo_repository.dart` 调用 `ApiClient`；页面/Widget 只持有 `XFile`/展示状态，不直接 `Dio`。

4. **选图：`image_picker`**  
   - **理由**：官方维护、跨 iOS/Android/Web（行为差异在实现时用 `kIsWeb` 等处理）。  
   - **备选**：`file_picker` — 对相机支持较弱，本场景以相机+相册为主。

5. **预览**  
   - 使用 `Image.network(url)` 或 `Image.network` + loading/error；清除时丢弃本地 `XFile` 与内存中的 `fileId/url`。

6. **路由（已定稿）**  
   - 新增**与 `StatefulShellRoute` 同级**的全屏 `GoRoute`，path **`/camera`**（`RoutePaths.camera`），页面即 **Camera 页**（不叫「上传页」）。  
   - **已登录**用户可访问；`redirect` 与现有门禁一致（未登录访问受保护路径则去登录页）。  
   - **入口**：首页 `CaptureCard`「拍一顿」`push` 至 `/camera`；可选：记录 Tab 占位内增加入口同样 `push`（Tab 根 builder **不**承载 Camera 全屏内容）。  
   - Camera 页使用 `Scaffold` + `AppBar`（或设计稿等价顶栏），**返回**即 `context.pop()`。

## Risks / Trade-offs

- **[Risk] Web（Chrome）上 `image_picker` / 相机权限与 CORS** → 开发期以相册为主验证；确保 `API_BASE_URL` 与浏览器 CORS 允许 `multipart`。  
- **[Risk] 大文件超时** → 依赖现有 `receiveTimeout`；若频繁失败再在配置中单独加长上传超时（后续变更）。  
- **[Risk] OSS 未配置时业务码非 0** → 沿用统一业务错误展示，文案可读即可。

## Migration Plan

- 纯客户端功能增量；无数据迁移。  
- **回滚**：还原路由与 `pubspec` 依赖即可。

## Open Questions

- （暂无）Camera 功能固定在独立路由 `/camera`；记录 Tab 是否增加入口按钮可在实现时与 UI 对齐，但不改变「Camera 为独立路由」的约束。
