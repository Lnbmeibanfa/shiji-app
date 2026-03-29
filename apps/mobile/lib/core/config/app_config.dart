import 'app_env.dart';

/// 应用级配置：仅允许从编译期 define 读取，禁止在业务代码中硬编码生产域名。
abstract final class AppConfig {
  static AppEnv get env => AppEnv.fromDefine();

  /// 后端 API 根地址，须通过 `--dart-define=API_BASE_URL=https://...` 注入。
  /// 默认指向本机，便于开发；生产构建必须由 CI/本地显式传入。
  static String get apiBaseUrl {
    const url = String.fromEnvironment(
      'API_BASE_URL',
      defaultValue: 'http://127.0.0.1:8080',
    );
    return url.endsWith('/') ? url.substring(0, url.length - 1) : url;
  }

  static const Duration connectTimeout = Duration(seconds: 15);
  static const Duration receiveTimeout = Duration(seconds: 30);
}
