import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'auth/auth_controller.dart';
import 'network/api_client.dart';
import 'network/auth_interceptor.dart';
import 'routing/app_router.dart' show createAppRouter;
import 'storage/auth_storage.dart';
import 'storage/secure_storage_facade.dart';
import '../features/auth/repositories/auth_repository.dart';

final secureStorageProvider = Provider<SecureStorageFacade>((ref) {
  return SecureStorageFacade();
});

final authStorageProvider = Provider<AuthStorage>((ref) {
  return AuthStorage(ref.watch(secureStorageProvider));
});

final dioProvider = Provider<Dio>((ref) {
  final dio = createDio();
  dio.interceptors.add(AuthInterceptor(ref.watch(authStorageProvider)));
  return dio;
});

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient(ref.watch(dioProvider));
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(ref.watch(apiClientProvider));
});

final authControllerProvider = ChangeNotifierProvider<AuthController>((ref) {
  final c = AuthController(storage: ref.watch(authStorageProvider));
  ref.onDispose(c.dispose);
  return c;
});

/// 全局 [GoRouter]；使用 `read` 绑定 [AuthController]，避免在登录态变化时重建 router 实例。
final routerProvider = Provider<GoRouter>((ref) {
  final auth = ref.read(authControllerProvider);
  final router = createAppRouter(auth);
  ref.onDispose(router.dispose);
  return router;
});
