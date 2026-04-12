# 食迹移动端（Flutter）

## 运行

```bash
cd apps/mobile
flutter pub get
```

### API 地址（`--dart-define`）

默认 `API_BASE_URL` 为 `http://127.0.0.1:8080`。模拟器访问本机后端请使用：

```bash
# Android 模拟器 → 宿主机
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080 --dart-define=APP_ENV=dev

# iOS 模拟器可用 localhost
flutter run --dart-define=API_BASE_URL=http://127.0.0.1:8080 --dart-define=APP_ENV=dev
```

生产/预发构建必须在 CI 或本地显式传入 `API_BASE_URL`，**勿**在 Dart 源码中写死生产域名。

登录页已实现短信验证码登录 UI，并调用 `AuthRepository` / `AuthController`；联调步骤仍以 `services/api/docs/auth-api.md` 与上文 `--dart-define` 为准。

### 后端与登录联调

认证接口说明见仓库 `services/api/docs/auth-api.md`。开发环境验证码见后端日志 `SMS stub sent`。

启动后端后，应用经 `Splash` → 无 token 进入登录页；登录成功写入 token 后由 `AuthController` + `GoRouter` 进入首页占位。首页可点「退出登录」验证门禁。

登录页内已调用 `loginWithSmsCode` 与 `setSessionToken`；若在其他场景手动集成，可沿用：

```dart
final login = await ref.read(authRepositoryProvider).loginWithSmsCode(
  phone: phone,
  code: code,
  agreements: agreements, // 与勾选一致，勿硬编码
);
await ref.read(authControllerProvider).setSessionToken(login.token);
```

**冷启动验证（手动）**：登录后杀进程再开，应仍进首页（token 从安全存储恢复）；退出后再开应进登录页。

### 记录饮食（`/record-meal`）与 OSS / 保存

登录后从首页「拍一顿」或「记录」Tab「上传照片」进入 **记录饮食** 全屏页：相册 / 相机选图后自动调用 `POST /api/files/upload`（multipart，字段 `file`），成功则展示 OSS 返回图 URL；填写餐别与至少一条食物后可调用 `POST /api/meal-records` 保存。网络层见 `ApiClient`，业务经 `MealPhotoRepository`、`MealRecordRepository`。

**Web（Chrome）联调**：若 `flutter run -d chrome` 调本机 API，需后端 CORS 允许该来源及对 `/api/files/upload` 的 `multipart` 预检；权限与相册行为与移动端不一致时以真机/模拟器为准。

## 架构说明（Riverpod + GoRouter）

- `lib/core/providers.dart`：注册 `AuthStorage`、`Dio`、`ApiClient`、`AuthRepository`、`MealPhotoRepository`、`MealRecordRepository`、`AuthController`、`GoRouter`。
- `AuthController` 实现 `ChangeNotifier`，作为 `GoRouter.refreshListenable`，登录态变化会触发 redirect。
- 业务页面与 Widget **禁止** `import 'package:dio/dio.dart'`；HTTP 仅通过 `AuthRepository`、`MealPhotoRepository`、`MealRecordRepository` 等封装访问（见 `docs/infrastructure.md`）。

详见 `openspec/changes/flutter-client-infrastructure-v1/design.md`。

Code Review 清单（禁止 feature 直连 Dio）：见 [docs/infrastructure.md](docs/infrastructure.md)。

## 测试

```bash
flutter analyze
flutter test
```

## 后续（P2）

- **CI**：已添加 `.github/workflows/flutter-mobile.yml`（变更 `apps/mobile` 时跑 `flutter analyze` / `flutter test`）。
- **i18n**：当前无 `intl` 依赖；若要做多语言，再引入 `flutter_localizations` + `intl`，并单独开变更收敛文案。
- **崩溃 / 埋点**：Firebase Crashlytics、Sentry 等选型后单独变更；须在隐私政策中披露收集范围。
