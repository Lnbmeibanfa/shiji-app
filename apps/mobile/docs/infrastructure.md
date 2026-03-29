# 基础设施与 Code Review 清单

## 禁止在 feature 中直接依赖 Dio

- `features/**` 内文件不得出现 `import 'package:dio/dio.dart'`。
- HTTP 调用经 `lib/core/network/api_client.dart` 与各 `repositories/*`。

审查时可在仓库根执行：

```bash
rg "package:dio/dio.dart" apps/mobile/lib/features
```

无输出即为通过（`core/network` 除外）。
