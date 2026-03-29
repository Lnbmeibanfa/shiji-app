import 'api_exceptions.dart';

typedef DataParser<T> = T Function(Object? raw);

/// 与后端 `ApiResponse` 对齐：`{ code, message, data }`。
abstract final class ApiEnvelope {
  /// 解析顶层 JSON：`code==0` 时用 [parseData] 解析 `data`；否则抛出 [ApiBusinessException]。
  static T parse<T>(Map<String, dynamic> json, DataParser<T> parseData) {
    final code = json['code'] as int? ?? -1;
    final message = json['message'] as String? ?? '';
    if (code != 0) {
      throw ApiBusinessException(code, message);
    }
    return parseData(json['data']);
  }
}
