import 'package:dio/dio.dart';

import '../config/app_config.dart';
import '../logging/app_logger.dart';
import 'api_exceptions.dart';
import 'api_response.dart';
import 'models/file_upload_response.dart';

typedef JsonMap = Map<String, dynamic>;

/// 唯一 HTTP 出口：统一 POST JSON 与响应解析。
class ApiClient {
  ApiClient(this._dio);

  final Dio _dio;

  Dio get dio => _dio;

  /// GET JSON，`code==0` 时将 `data` 交给 [parseData]。
  Future<T> getJson<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    required DataParser<T> parseData,
  }) async {
    try {
      final response = await _dio.get<Object?>(
        path,
        queryParameters: queryParameters,
      );
      final raw = response.data;
      if (raw is! Map<String, dynamic>) {
        throw ApiHttpException(response.statusCode, '响应不是 JSON 对象');
      }
      return ApiEnvelope.parse<T>(raw, parseData);
    } on ApiBusinessException {
      rethrow;
    } on DioException catch (e) {
      AppLogger.apiError('GET $path', e);
      final status = e.response?.statusCode;
      final msg = e.message ?? '网络请求失败';
      throw ApiHttpException(status, msg);
    }
  }

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

  /// 餐食照片字节上传（`POST /api/files/upload`，字段 `file`）。
  Future<FileUploadResponse> uploadMealPhotoBytes({
    required List<int> bytes,
    required String filename,
  }) {
    final file = MultipartFile.fromBytes(bytes, filename: filename);
    return postMultipartFile(
      '/api/files/upload',
      fieldName: 'file',
      file: file,
    );
  }

  /// `multipart/form-data` 上传，`[fieldName]` 对应后端 `@RequestParam("file")` 等。
  /// 使用 [FormData] 时由 Dio 设置带 boundary 的 Content-Type，故不沿用 BaseOptions 的 JSON Content-Type。
  Future<FileUploadResponse> postMultipartFile(
    String path, {
    required String fieldName,
    required MultipartFile file,
  }) async {
    final formData = FormData.fromMap(<String, dynamic>{fieldName: file});
    try {
      final response = await _dio.post<Object?>(
        path,
        data: formData,
        // 覆盖 BaseOptions 的 application/json；发送前 dio 会为 FormData 写成带 boundary 的 multipart。
        options: Options(
          contentType: Headers.multipartFormDataContentType,
        ),
      );
      final raw = response.data;
      if (raw is! Map<String, dynamic>) {
        throw ApiHttpException(response.statusCode, '响应不是 JSON 对象');
      }
      return ApiEnvelope.parse<FileUploadResponse>(
        raw,
        FileUploadResponse.fromJson,
      );
    } on ApiBusinessException {
      rethrow;
    } on DioException catch (e) {
      AppLogger.apiError('POST multipart $path', e);
      final status = e.response?.statusCode;
      final msg = e.message ?? '网络请求失败';
      throw ApiHttpException(status, msg);
    }
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
