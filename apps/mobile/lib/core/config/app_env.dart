/// 运行环境（由 `--dart-define=APP_ENV=...` 注入，默认 dev）。
enum AppEnv {
  dev,
  staging,
  prod;

  static AppEnv fromDefine() {
    const v = String.fromEnvironment('APP_ENV', defaultValue: 'dev');
    switch (v) {
      case 'staging':
        return AppEnv.staging;
      case 'prod':
        return AppEnv.prod;
      default:
        return AppEnv.dev;
    }
  }
}
