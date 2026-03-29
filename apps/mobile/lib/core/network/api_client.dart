import 'package:dio/dio.dart';

import '../config/app_config.dart';
import '../logging/app_logger.dart';
import 'api_exceptions.dart';
import 'api_response.dart';

typedef JsonMap = Map<String, dynamic>;

/// 唯一 HTTP 出口：统一 POST JSON 与响应解析。
class ApiClient {
  ApiClient(this._dio);

  final Dio _dio;

  Dio get dio => _dio;

  /// POST JSON，`code==0` 时将 `data` 交给 [parseData]。
  Future<T> postJson<T>(
    String path, {
    JsonMap? body,
    required DataParser<T> parseData,
  }) async {
    try {
      final response = await _dio.post<Object?>(
        path,
        data: body,
      );
      final raw = response.data;
      if (raw is! Map<String, dynamic>) {
        throw ApiHttpException(response.statusCode, '响应不是 JSON 对象');
      }
      return ApiEnvelope.parse<T>(raw, parseData);
    } on ApiBusinessException {
      rethrow;
    } on DioException catch (e) {
      AppLogger.apiError('POST $path', e);
      final status = e.response?.statusCode;
      final msg = e.message ?? '网络请求失败';
      throw ApiHttpException(status, msg);
    }
  }

  /// 无业务 data（如发码、登出），成功时仅校验 `code==0`。
  Future<void> postVoid(
    String path, {
    JsonMap? body,
  }) async {
    await postJson<Object?>(
      path,
      body: body,
      parseData: (_) => null,
    );
  }
}

/// 构建带 baseUrl 与超时的 [Dio]（拦截器由调用方追加）。
Dio createDio() {
  return Dio(
    BaseOptions(
      baseUrl: AppConfig.apiBaseUrl,
      connectTimeout: AppConfig.connectTimeout,
      receiveTimeout: AppConfig.receiveTimeout,
      headers: <String, dynamic>{
        Headers.contentTypeHeader: Headers.jsonContentType,
        Headers.acceptHeader: Headers.jsonContentType,
      },
    ),
  );
}
