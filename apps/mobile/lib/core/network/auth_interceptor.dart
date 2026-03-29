import 'package:dio/dio.dart';

import '../storage/auth_storage.dart';

/// 为需要登录态的请求注入 `Authorization: Bearer`。
class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._authStorage);

  final AuthStorage _authStorage;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _authStorage.readToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }
}
