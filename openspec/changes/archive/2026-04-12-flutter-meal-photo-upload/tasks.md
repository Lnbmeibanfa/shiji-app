## 1. 依赖与模型

- [x] 1.1 在 `apps/mobile/pubspec.yaml` 增加 `image_picker`（及必要时 `cross_file`/`mime` 若 Dio multipart 需要），执行 `flutter pub get`
- [x] 1.2 新增 `FileUploadResponse`（或等价）模型：`fileId`、`url`、`objectKey`、`bucket`、`contentType`、`size`，与后端 `FileUploadResponse` 字段对齐，含 `fromJson`

## 2. 网络层

- [x] 2.1 在 `ApiClient` 中新增 multipart 上传方法：内部使用 `FormData` + `MultipartFile`，`POST` `/api/files/upload`，字段名 `file`；响应走 `ApiEnvelope.parse` 解析为 `FileUploadResponse`
- [x] 2.2 确认 `Dio` 默认 `Content-Type` 不会破坏 multipart：上传请求应移除或覆盖为让 Dio 自动带 boundary（必要时对该次请求使用 `Options`）
- [x] 2.3 为上传方法补充日志/错误路径，与现有 `postJson` 的 `DioException` 处理风格一致

## 3. Feature 与状态

- [x] 3.1 新增 `MealPhotoRepository`（或 `FileUploadRepository`）封装上传调用，仅依赖 `ApiClient`
- [x] 3.2 在 `providers.dart` 注册 Repository 的 `Provider`
- [x] 3.3 实现 **Camera 页** Widget（对应路由 `/camera`）：相册按钮、相机按钮、上传中防重、成功 `Image.network` 预览、清除重选、失败 SnackBar（沿用 `AppSnackbar` 等现有反馈）

## 4. 路由与集成

- [x] 4.1 在 `route_paths.dart` 新增 `camera = '/camera'`（或 `RoutePaths.camera`），在 `app_router.dart` 注册**与 Shell 同级**的全屏 `GoRoute`，`builder` 指向 **Camera 页**；`redirect` 中已登录门禁与现有规则一致
- [x] 4.2 首页 `CaptureCard`「拍一顿」`onTap` 使用 `context.push` 导航至 `/camera`；可选：记录 Tab 占位增加按钮同样 `push`（**勿**把 Camera 页设为 `/record` Tab 的 `builder` 根视图）
- [x] 4.3 Camera 页提供顶栏返回（`pop`）。手动验证：真机或模拟器相册 + 上传成功预览；Web 上相册/CORS 已知限制可记入 README 或任务备注

## 5. 规范落地

- [x] 5.1 变更完成后执行 `openspec archive` 流程前，确认 `meal-photo-upload-ui` 与 `flutter-http-contract` 增量行为已实现
