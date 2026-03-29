/// HTTP 层错误（非业务 code）。
class ApiHttpException implements Exception {
  ApiHttpException(this.statusCode, [this.message]);

  final int? statusCode;
  final String? message;

  @override
  String toString() => 'ApiHttpException($statusCode, $message)';
}

/// 后端返回 `code != 0`（HTTP 可能仍为 200）。
class ApiBusinessException implements Exception {
  ApiBusinessException(this.code, this.message);

  final int code;
  final String message;

  @override
  String toString() => 'ApiBusinessException($code, $message)';
}
